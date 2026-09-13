# Day 32 Security Threat Model: Multi-Tenant Security & Customer IDOR Hardening

## 1. Executive Summary & Golden Security Principle

In a multi-tenant SaaS application, authentication and authorization solve two distinct problems:
- **Authentication**: Answers *Who are you?* (established via cryptographically signed JWT tokens).
- **Authorization & Isolation**: Answers *What data are you allowed to access?* (enforced via server-side tenant scoping and object-level ownership).

Prior to Day 32, components relied on client-supplied headers (`X-Tenant-Id`, `X-Customer-Id`, `X-User-Role`, `X-User-Name`) and defaulted to fallback tenants (`EVERGREEN_TENANT_ID`) or default roles (`OWNER`). This architecture presented critical vulnerabilities where an authenticated user of Tenant A could manipulate HTTP request headers or URL paths to view, mutate, or link data belonging to Tenant B, or where Customer A could access Customer B's private event, booking, quote, or invoice data.

Day 32 enforces the Golden Security Principle:
```text
JWT Signature Validation
       ↓
Authenticated Principal (RentFlowPrincipal)
       ↓
Server-Derived Tenant & Customer Identity
       ↓
TenantContext (ThreadLocal with guaranteed cleanup)
       ↓
Service Authorization
       ↓
Tenant-Scoped Database Queries
       ↓
Database Engine
```

Client-supplied identity headers (`X-Tenant-Id`, `X-Customer-Id`, `X-User-Role`) are strictly stripped of all authoritative power.

---

## 2. Threat Model & Attack Scenarios

### Attack Scenario 1: Tenant ID Header Forgery (Tenant Spoofing)
- **Vector**: An authenticated user of Tenant A sends a valid JWT for Tenant A, but attaches `X-Tenant-Id: <Tenant-B-UUID>` to HTTP requests.
- **Vulnerability**: Controllers previously resolved tenant identity by checking `headerTenantId != null ? headerTenantId : EVERGREEN_TENANT_ID`.
- **Impact**: Tenant A could view or manipulate Tenant B's bookings, quotes, inventory, and customers.
- **Day 32 Mitigation**: Controllers and services derive the active tenant strictly from `CurrentUserService.requireTenantId()`, which inspects the verified `RentFlowPrincipal`. Any `X-Tenant-Id` header is ignored or rejected if conflicting.

### Attack Scenario 2: Direct Object Reference (URL IDOR)
- **Vector**: Tenant A authenticates and requests `GET /api/bookings/{tenant-b-booking-id}`.
- **Vulnerability**: Internal lookup using `repository.findById(id)` without scoping to `tenant_id`.
- **Impact**: Full disclosure of Tenant B customer details, rental dates, inventory items, and pricing.
- **Day 32 Mitigation**: All resource lookups enforce `repository.findByTenantIdAndId(tenantId, id)`. If the resource does not exist within the caller's tenant, the system returns **404 Not Found**.

### Attack Scenario 3: Cross-Tenant Resource Mutation (Update IDOR)
- **Vector**: Tenant A issues `PUT /api/customers/{tenant-b-customer-id}` or `PUT /api/quotes/{tenant-b-quote-id}` with Tenant A's JWT.
- **Vulnerability**: Target entity loaded without tenant verification, or request body containing `tenantId` accepted and copied to database entity.
- **Impact**: Tampering with another tenant's customer records, pricing, or status.
- **Day 32 Mitigation**: Updates load the target entity via `(tenantId, id)` and throw 404 if not found. Furthermore, DTO deserialization does not allow the client to alter `tenantId`.

### Attack Scenario 4: Cross-Tenant Resource Deletion
- **Vector**: Tenant A issues `DELETE /api/products/{tenant-b-product-id}`.
- **Vulnerability**: Invoking `productRepository.deleteById(id)` without verifying that the product belongs to Tenant A.
- **Impact**: Denial of service and unauthorized data deletion across tenant boundaries.
- **Day 32 Mitigation**: Deletions first query `productRepository.findByTenantIdAndId(tenantId, id)` (throwing 404 if absent), or execute a tenant-scoped delete query.

### Attack Scenario 5: Relationship Injection (Foreign Key Tampering)
- **Vector**: Tenant A creates a Quote or Booking referencing a Customer or Product ID belonging to Tenant B:
  ```json
  {
    "customerId": "tenant-b-customer-id",
    "items": [{ "productId": "tenant-b-product-id", "quantity": 2 }]
  }
  ```
- **Vulnerability**: Backend validates that referenced IDs exist in the database globally (`findById`), but fails to verify that they belong to the same tenant as the quote/booking.
- **Impact**: Data corruption, cross-tenant leakage where Tenant A sees Tenant B's customer name or product catalog.
- **Day 32 Mitigation**: All referenced foreign entities (`Customer`, `Product`, `Event`, `Quote`) are resolved using `findByTenantIdAndId(tenantId, foreignId)`. If any foreign entity does not belong to the caller's tenant, the request is rejected with 400 Bad Request / 404 Not Found.

### Attack Scenario 6: Customer Portal IDOR (BOLA)
- **Vector**: Customer A1 logs into the customer portal, obtains a valid customer JWT, and queries:
  `GET /api/portal/bookings/{customer-a2-booking-id}`
  or sends `X-Customer-Id: <Customer-A2-UUID>`.
- **Vulnerability**: Portal controller resolved customer identity from `X-Customer-Id` header (falling back to a hardcoded demo customer `EMILY_CUSTOMER_ID`), or portal service did not verify customer ownership.
- **Impact**: Customers could access other customers' quotes, invoices, payments, contracts, damage claims, and personal identifiable information (PII).
- **Day 32 Mitigation**:
  1. `X-Customer-Id` header is completely removed from portal controllers.
  2. Customer ID is extracted directly from the verified `RentFlowPrincipal.getCustomerId()`.
  3. Every portal query enforces `tenantId` AND `customerId` matching. If an entity does not belong to the authenticated customer, the API returns **404 Not Found**.
  4. Auto-creation of fallback demo customers in `getCustomerWithAuth` is permanently removed.

---

## 3. Defense-in-Depth Architecture

1. **Spring Security Filter Chain**:
   `JwtAuthenticationFilter` validates token signature, expiration, and extracts `RentFlowPrincipal`.
2. **Tenant Context Synchronization**:
   `TenantContextFilter` runs immediately after authentication and synchronizes `SecurityUtils` ThreadLocal context. A `finally` block guarantees that ThreadLocal state is cleared upon request completion, preventing context leakage in servlet thread pools.
3. **CurrentUserService Layer**:
   `CurrentUserService` serves as the single source of truth for obtaining authenticated `userId`, `tenantId`, `role`, and `customerId`. Throws explicit security exceptions if unauthenticated.
4. **Tenant-Scoped Repositories**:
   Database access uses `findByTenantIdAndId`, `findByTenantId`, and explicit `WHERE tenant_id = :tenantId` queries.
5. **Response Strategy (404 vs 403)**:
   - For attempts to access cross-tenant or cross-customer resource IDs: **404 Not Found** (prevents ID enumeration and existence disclosure).
   - For role-based denials (e.g. `CUSTOMER` accessing `/api/admin/**` or `/api/customers`): **403 Forbidden**.

---

## 4. Background Schedulers & AI Agents

- **Schedulers**: Scheduled jobs lack an HTTP request context. They must explicitly iterate tenants, set bounded context via `SecurityUtils.setContext(tenantId, "SYSTEM", "scheduler")`, perform processing within a `try-finally` block, and always clear the context in `finally`.
- **AI Sales & Copilot Tools**: All AI tool executions must derive tenant context from the trusted user session, never accepting an arbitrary `tenantId` parameter from LLM-generated tool call arguments.
