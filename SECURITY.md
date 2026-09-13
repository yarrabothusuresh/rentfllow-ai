# RentFlow AI — Production Security Architecture & Policy

## 1. Security Overview & Philosophy
RentFlow AI is an enterprise-grade, multi-tenant SaaS rental management platform with embedded AI capabilities (AI Sales Agent, Internal AI Copilot, Recommendations/Automation, and Phone AI Foundation). 

Security in RentFlow AI is built on the following foundational tenets:
- **Defense in Depth**: Security checks are enforced across network boundaries, HTTP transport filters, reverse proxy headers, application services, and database query filters.
- **Tenant Isolation as a Zero-Tolerance Boundary**: Every entity has a non-nullable `tenantId`. Data access is never global; every JPA repository query and mutating operation is strictly tenant-scoped.
- **AI Is Never The Source of Truth**: AI services (LLM, prompt generation, tool calls) cannot modify databases directly, confirm bookings autonomously, process raw credit card numbers over voice, or bypass business logic constraints.
- **Least Privilege & Role-Based Access Control (RBAC)**: Fine-grained permissions are assigned to specific roles (`ADMIN`, `SALES`, `OPERATIONS`, `WAREHOUSE`, `DRIVER`, `CUSTOMER`).

---

## 2. Multi-Tenant Isolation
RentFlow AI enforces logical multi-tenancy across all operational domains:

| Entity Layer | Isolation Mechanism | Verification Test |
| :--- | :--- | :--- |
| **Catalog & Inventory** | Scoped by `tenantId` in `findByTenantIdAndId`, `searchProducts` | `Day30CrossTenantSecurityTest` |
| **CRM & Customers** | Scoped by `tenantId` in `findByTenantIdAndId` | `Day30CrossTenantSecurityTest` |
| **Quotes & Bookings** | Scoped by `tenantId` on repository level; state machine checks | `Day30CrossTenantSecurityTest` |
| **Invoices & Payments** | Scoped by `tenantId`; immutable transaction references | `Day30CrossTenantSecurityTest` |
| **Warehouse & Fulfillment**| Orders, pick lists, and load lists strictly isolated by tenant | `Day30CrossTenantSecurityTest` |
| **Deliveries & Routes** | Dispatches, driver assignments, and vehicle telemetry tenant-bound | `Day30CrossTenantSecurityTest` |
| **Damage Claims & Returns**| Claims, inspection reports, and repair costs isolated | `Day30CrossTenantSecurityTest` |
| **AI Signals & Automation** | Recommendations, triggers, and idempotency keys isolated | `Day30CrossTenantSecurityTest` |
| **Phone AI & Telephony** | Call sessions, recordings, and transcripts isolated by `tenantId` | `Day30CrossTenantSecurityTest` |

---

## 3. Production Hardening & Safe Defaults
### Fail-Fast Startup Validation (`SecurityStartupValidator`)
In the `prod` profile, the application will **fail to start** if insecure configurations are detected:
- **JWT Secret Entropy**: Rejects default, empty, or short keys (< 32 characters or containing `"change"`, `"secret"`, `"123456"`).
- **CORS Allowed Origins**: Rejects wildcard `*` or empty origins when credentials are enabled. Explicit origins must be supplied via `CORS_ALLOWED_ORIGINS`.

### HTTP Security Headers Filter (`SecurityHeadersFilter`)
All responses automatically include:
- `Content-Security-Policy`: `default-src 'self'; script-src 'self'; style-src 'self' 'unsafe-inline'; img-src 'self' data: https:; font-src 'self' data:; connect-src 'self'; frame-ancestors 'none';`
- `Strict-Transport-Security`: `max-age=31536000; includeSubDomains; preload`
- `X-Content-Type-Options`: `nosniff`
- `X-Frame-Options`: `DENY`
- `Referrer-Policy`: `strict-origin-when-cross-origin`
- `Permissions-Policy`: `camera=(), microphone=(), geolocation=()`

### Rate Limiting (`RateLimitingService`)
- In-memory token bucket rate limiting on sensitive public endpoints (`/api/auth/login`, `/api/ai-sales/chat`, `/api/phone/simulate-inbound`).
- IP-based and key-based sliding windows preventing denial-of-service and brute force credential attacks.

### SSRF Protection (`SsrfProtectionValidator`)
- Enforced on all outbound webhook dispatchers and integration sync endpoints.
- Validates hostname and resolved IP addresses against IPv4/IPv6 private ranges (RFC 1918), loopback (`127.0.0.0/8`, `::1`), link-local (`169.254.0.0/16`), and cloud metadata IP endpoints (`169.254.169.254`).
- Restricts protocols strictly to `http` and `https`.

---

## 4. AI Security & Guardrails
1. **Prompt Injection Defense**:
   - System prompts explicitly enforce persona boundaries, reject system instruction overrides, and sanitize delimiter attacks (`===`, ````, `{SYSTEM}`).
2. **Tool Authorization**:
   - All AI tools execute through deterministic Java service layers (`AiSalesToolRegistry`, `CopilotActionService`, `AutomationActionDispatcher`).
   - Copilot and automation actions require explicit human confirmation before state mutation.
3. **Voice AI Guardrails**:
   - Phone AI cannot collect credit card numbers, CVVs, or bank routing numbers over voice. Callers are directed to a secure customer portal payment link.
   - Immediate human handoff is triggered upon caller demand or repeated intent misunderstanding.
   - Recording and transcription consent disclosures are played prior to AI processing.

---

## 5. Authentication Foundation (Day 31)

### Real Authenticated Identities (Spring Security + BCrypt + JWT)
RentFlow AI replaced all demo and header-spoofing identity mechanisms with cryptographically signed, stateless JSON Web Tokens (JWT) using JJWT 0.12.5 and Spring Security 6:

1. **Stateless Security Pipeline**:
   - `JwtAuthenticationFilter` intercepts all incoming requests, extracts the Bearer token from the `Authorization` header, verifies the HMAC-SHA256 signature, validates token expiration and issuer, and constructs an immutable `RentFlowPrincipal`.
   - `SecurityContextHolder` is populated with `UsernamePasswordAuthenticationToken` containing `RentFlowPrincipal` and `SimpleGrantedAuthority("ROLE_" + role)`.
   - `TenantContextFilter` synchronously sets `TenantContext.setCurrentTenant(principal.getTenantId())` exclusively from the verified principal.

2. **Zero-Trust Context Enforcement**:
   - `SecurityUtils` has had all silent default fallbacks to `"evergreen-rentals"` and `"OWNER"` permanently removed.
   - Any attempt to invoke secured tenant services without an authenticated principal throws `AuthenticationCredentialsNotFoundException`, resulting in an immediate structured `401 Unauthorized`.
   - Untrusted request headers (`X-Tenant-Id`, `X-User-Role`, `X-Customer-Id`) cannot override or spoof the identity inside the verified JWT.

3. **JWT Claims Architecture**:
   - `sub`: User ID / Customer User ID (UUID string)
   - `tenantId`: Tenant ID string
   - `email`: Normalized lowercase email address
   - `role`: Role string (`OWNER`, `ADMIN`, `SALES`, `OPERATIONS`, `WAREHOUSE`, `DRIVER`, `CUSTOMER`)
   - `customerId`: Customer UUID string (present on `CUSTOMER` portal tokens)
   - `type`: Token type (`ACCESS_TOKEN`)
   - `iss`: `rentflow-ai`
   - `iat` / `exp`: Issued at and expiration timestamps (default 8 hours)

4. **BCrypt Password Security & Timing Attack Mitigation**:
   - All passwords hashed using `BCryptPasswordEncoder` with strength `12`.
   - `AuthenticationService` executes dummy BCrypt verification if a requested user does not exist, guaranteeing constant-time response profiles to thwart account enumeration and timing attacks.
   - Passwords and password hashes are strictly omitted from `toString()`, JSON serializers, DTOs, and logging output.

5. **Rate Limiting**:
   - Login attempts (`/api/auth/login` and `/api/portal/auth/login`) are rate-limited per client IP (5 attempts per minute). Exceeding this threshold returns `429 Too Many Requests`.

6. **Customer Portal Authentication**:
   - Customer authentication is strictly isolated at `/api/portal/auth/login` and `/api/portal/auth/register`.
   - Issues verified JWTs with `role: CUSTOMER` and embedded `customerId`.
   - Legacy mock tokens (`demo-portal-token-...`) are completely deprecated and rejected by `SecurityConfig`.

---

## 6. Multi-Tenant Security & Customer IDOR Hardening (Day 32)

### 1. Anti-Header-Spoofing Architecture
- **Zero Trust for Client Identity Headers**: Raw client headers (`X-Tenant-Id`, `X-User-Role`, `X-User-Name`, `X-Customer-Id`) are strictly untrusted on all authenticated endpoints.
- **Authoritative Identity Flow**:
  ```text
  Client Request (Authorization: Bearer <JWT>)
    ↓
  JwtAuthenticationFilter (validates signature, exp, issuer)
    ↓
  RentFlowPrincipal (holds verified userId, tenantId, email, role, customerId)
    ↓
  SecurityContextHolder
    ↓
  TenantContextFilter (ThreadLocal context with guaranteed finally { clearContext() })
    ↓
  CurrentUserService (provides fail-closed resolution to controllers & services)
  ```
- **Unauthenticated Fallback Deprecation**: All legacy fallbacks to demo tenants (e.g., `"evergreen"`) and default `"OWNER"` privileges have been eliminated. Missing or unverified credentials trigger immediate `AuthenticationCredentialsNotFoundException` or `401 Unauthorized`.

### 2. Information Disclosure Prevention (404 Not Found Policy)
- Cross-tenant reads, updates, and deletes return **`404 Not Found`** rather than `403 Forbidden`. Foreign tenant resource lookups behave identically to non-existent resources, preventing attackers from confirming or enumerating valid entity IDs across tenant boundaries.
- Cross-tenant relationship injection (e.g., Tenant A submitting a quote or booking referencing a Customer ID from Tenant B) is caught and rejected with `400 Bad Request` before database persistence.

### 3. Customer Portal IDOR Elimination
- All Customer Portal endpoints (`/api/portal/**`) resolve `tenantId` and `customerId` directly from `CurrentUserService`.
- Client requests cannot alter their targeted customer scope by sending spoofed headers (`X-Customer-Id`); the server strictly uses the cryptographically bound `customerId` claim from the JWT.
- Attempts by Customer A to read or manipulate Customer B's quotes, bookings, invoices, or damage claims result in `404 Not Found`.
- `ROLE_CUSTOMER` is restricted at the `SecurityConfig` layer from accessing staff routes (`/api/customers/**`, `/api/admin/**`, `/api/warehouse/**`, `/api/bookings/**`), returning `403 Forbidden`.

### 4. Automated Security Test Matrix
- `CrossTenantSecurityTest`: 7 automated tests proving spoofed header immunity, cross-tenant 404s on read/update/delete, relationship injection prevention, tenant-scoped search/list, and ThreadLocal cleanup.
- `CustomerPortalIdorSecurityTest`: 7 automated tests proving customer portal IDOR immunity across quotes, bookings, invoices, damage claims, header spoofing resistance, and staff endpoint rejection.

---

## 7. Manual Verification via PowerShell

```powershell
# 1. Verify Unauthenticated Request returns 401
Invoke-RestMethod -Uri "http://localhost:8080/api/bookings" -Method Get -SkipHttpErrorCheck

# 2. Verify Staff Login returns JWT
$loginBody = @{ email = "owner@demo.local"; password = "ChangeMe123!" } | ConvertTo-Json
$auth = Invoke-RestMethod -Uri "http://localhost:8080/api/auth/login" -Method Post -ContentType "application/json" -Body $loginBody
$token = $auth.accessToken

# 3. Verify Authenticated Request succeeds (Header spoofing ignored)
Invoke-RestMethod -Uri "http://localhost:8080/api/bookings" -Method Get -Headers @{ 
    Authorization = "Bearer $token"
    "X-Tenant-Id" = "malicious-foreign-tenant-id"
}

# 4. Verify Customer Portal Login
$portalLogin = @{ email = "customer@abcevents.demo"; password = "demo" } | ConvertTo-Json
$portalAuth = Invoke-RestMethod -Uri "http://localhost:8080/api/portal/auth/login" -Method Post -ContentType "application/json" -Body $portalLogin
$portalToken = $portalAuth.token

# 5. Verify Customer Portal Dashboard with Portal Token
Invoke-RestMethod -Uri "http://localhost:8080/api/portal/dashboard" -Method Get -Headers @{ Authorization = "Bearer $portalToken" }

# 6. Verify Customer Cannot Access Staff Endpoints (returns 403)
Invoke-RestMethod -Uri "http://localhost:8080/api/customers" -Method Get -Headers @{ Authorization = "Bearer $portalToken" } -SkipHttpErrorCheck
```

---

## 8. Vulnerability Reporting
For security concerns or vulnerability disclosures, contact security@rentflow.ai.

