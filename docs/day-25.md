# Day 25 — Integrations, Webhooks, External API Platform & Connector Foundation

## 1. Objective
RentFlow AI Day 25 establishes a unified, reliable, and secure Integration and External API Platform. It enables RentFlow AI to communicate seamlessly with third-party software (Accounting, CRM, E-commerce, Automations like Zapier/Make/n8n, and external developer clients) through:
1. **Transactional Outbox & Event Pipeline**: Decoupled, asynchronous event dispatch that guarantees core rental bookings, invoices, and payments succeed even if external services or webhook endpoints experience downtime.
2. **Outbound Webhooks**: HMAC-SHA256 signature signing, replay protection, exponential retry backoff, delivery logging, manual retry, and dead-letter queueing.
3. **External API Platform**: Secure `rf_live_...` API keys with hashed storage, granular RBAC scopes (`products:read`, `bookings:write`, etc.), tenant resolution, and versioned external REST endpoints (`/api/v1/external/...`).
4. **Connector Abstractions**: Pluggable connector interfaces for Accounting (QuickBooks/Xero), CRM (HubSpot/Salesforce), E-Commerce (Shopify), and Automations, backed by mock reference implementations and external entity mappings.
5. **Inbound Webhook Foundation**: Signature validation and strict idempotency across provider, external event ID, and tenant.

---

## 2. Existing Architecture Findings

| Component | Findings & Reusability | Day 25 Strategy |
| :--- | :--- | :--- |
| **Domain Events** | Spring `ApplicationEvent` infrastructure used for notifications (`BookingConfirmedEvent`, `InvoiceCreatedEvent`, `PaymentReceivedEvent`, `QuoteAcceptedEvent`, etc.) | Listen via `IntegrationEventListener` to convert domain events into transactional outbox records without blocking core business transactions. |
| **Tenant Isolation** | All entities partitioned by `tenantId` (UUID/String) with `SecurityUtils.getCurrentTenantId()` | All integrations, webhooks, deliveries, API keys, sync jobs, and external mappings are strictly tenant-scoped. |
| **Permissions & RBAC** | Role and permission model (`PermissionCode`, `RoleType`) | Added `INTEGRATION_VIEW`, `INTEGRATION_MANAGE`, `API_KEY_MANAGE`, `WEBHOOK_MANAGE` permissions. |
| **Credential Handling** | Credentials must never be logged or returned in plain text to Angular UI. | Hashed storage for API keys (`SHA-256`), masked UI displays (`••••••••1234`), and encrypted credential storage interfaces. |
| **External DTOs** | JPA entities must not be exposed directly to avoid circular references and data leakage. | Dedicated privacy-safe external DTOs that strip internal costs, employee internal notes, passwords, and sensitive card details. |

---

## 3. Integration & Outbox Architecture

```
                      RENTFLOW CORE DOMAIN TRANSACTION
        (e.g., BookingService, InvoiceService, PaymentService)
                                 │
                 ┌───────────────┴───────────────┐
                 ▼                               ▼
       Core Entity Insert / Update     Integration Outbox Insert
       (Commit together inside standard database transaction boundary)
                                 │
                                 ▼
                     TRANSACTIONAL OUTBOX TABLE
                                 │
                                 ▼ (Async Worker / Event Publisher)
                    INTEGRATION EVENT PIPELINE
                                 │
                 ┌───────────────┴───────────────┐
                 ▼                               ▼
         WEBHOOK DISPATCHER              CONNECTOR ENGINE
       (HMAC-SHA256 Signed POST)       (Mock Accounting / CRM)
                 │                               │
                 ▼                               ▼
         EXTERNAL CONSUMER              EXTERNAL SYSTEM MAPPING
      (Zapier / n8n / Webhook)          (QuickBooks / HubSpot ID)
```

---

## 4. Connector Abstraction & Capability Matrix

RentFlow defines a unified connector abstraction `IntegrationConnector` with specialization for domain-specific integrations:
- `AccountingConnector`: Customer, Invoice, Payment, Refund synchronization.
- `CrmConnector`: Customer, Lead, Quote, Booking, Activity synchronization.
- `CommerceConnector`: Product catalog sync, availability querying, inbound order conversion.

### Connector Capability Matrix

| Capability | Mock Accounting (QuickBooks) | Mock CRM (HubSpot) | Custom Webhook | Shopify (Future) |
| :--- | :---: | :---: | :---: | :---: |
| `CUSTOMERS` | ✅ Sync Customer | ✅ Sync Customer/Lead | ✅ Event payload | ✅ Customer sync |
| `PRODUCTS` | — | — | ✅ Event payload | ✅ Catalog sync |
| `QUOTES` | — | ✅ Deal/Quote sync | ✅ Event payload | — |
| `BOOKINGS` | — | ✅ Closed/Won Deal | ✅ Event payload | ✅ Order import |
| `INVOICES` | ✅ Sync Invoice | — | ✅ Event payload | — |
| `PAYMENTS` | ✅ Sync Payment | — | ✅ Event payload | — |
| `REFUNDS` | ✅ Sync Credit/Refund | — | ✅ Event payload | — |
| `INVENTORY` | — | — | ✅ Event payload | ⚠️ Rental date aware |
| `WEBHOOKS` | — | — | ✅ HMAC delivery | — |

---

## 5. Domain Events & Standard Envelope Format

Integration payloads are wrapped in a standard versioned envelope (`IntegrationEnvelopeDTO`):

```json
{
  "eventId": "evt_019543ab-7890-7def-a123-456789abcdef",
  "eventType": "booking.created",
  "eventVersion": "1.0",
  "tenantId": "d0e1a2b3-c4d5-6e7f-8a9b-0c1d2e3f4a5b",
  "occurredAt": "2026-09-01T22:30:00Z",
  "data": {
    "bookingId": "8f8b8941-b06d-4952-a5e2-2a7e7dfb6ef1",
    "bookingNumber": "BKG-000101",
    "customerId": "9a1b2c3d-4e5f-6a7b-8c9d-0e1f2a3b4c5d",
    "customerName": "Apex Luxury Events",
    "status": "CONFIRMED",
    "rentalStartDateTime": "2026-09-20T08:00:00",
    "rentalEndDateTime": "2026-09-22T20:00:00",
    "totalAmount": 2840.00,
    "itemCount": 4
  }
}
```

### Supported Integration Event Types
- `customer.created`, `customer.updated`
- `product.created`, `product.updated`
- `quote.created`, `quote.approved`, `quote.sent`
- `booking.created`, `booking.updated`, `booking.cancelled`
- `invoice.created`, `invoice.updated`, `invoice.sent`
- `payment.received`, `payment.failed`, `refund.completed`
- `inventory.updated`
- `delivery.completed`
- `return.completed`
- `damage_claim.created`
- `webhook.test`

---

## 6. Outbound Webhooks & HMAC-SHA256 Signatures

### Outbound HTTP Headers
Every outbound webhook includes the following authentication and traceability headers:
- `X-RentFlow-Event-Id`: Unique event UUID for consumer idempotency and deduplication.
- `X-RentFlow-Event-Type`: Dot-notated event name (e.g., `booking.created`).
- `X-RentFlow-Timestamp`: ISO-8601 UTC timestamp of dispatch.
- `X-RentFlow-Signature`: Hex-encoded `HMAC-SHA256(secret, timestamp + "." + payload)`.
- `Content-Type`: `application/json`.
- `User-Agent`: `RentFlow-Webhook-Dispatcher/1.0`.

### Consumer Verification Algorithm
1. Read the `X-RentFlow-Timestamp` and `X-RentFlow-Signature` headers.
2. Verify timestamp is within a 5-minute tolerance window to prevent replay attacks.
3. Compute `HMAC-SHA256(signingSecret, timestamp + "." + rawBody)`.
4. Use constant-time equality comparison between computed HMAC and `X-RentFlow-Signature`.
5. Store and deduplicate on `X-RentFlow-Event-Id` for at-least-once delivery idempotency.

---

## 7. Retry Policy, Backoff & Dead-Letter Queue

### Exponential Backoff Schedule
| Attempt Number | Delay Before Retry | Action |
| :---: | :---: | :--- |
| **Attempt 1** | Immediate / 1s | Initial HTTP POST |
| **Attempt 2** | 1 minute | Retry Attempt 1 |
| **Attempt 3** | 5 minutes | Retry Attempt 2 |
| **Attempt 4** | 30 minutes | Retry Attempt 3 |
| **Attempt 5** | 2 hours | Final Retry Attempt |
| **Exhausted** | — | Transition to `DEAD_LETTER` status |

### Retryable vs. Non-Retryable HTTP Status Codes
- **Retryable**: Connection timeouts, DNS failures, HTTP 408 (Request Timeout), HTTP 429 (Too Many Requests), HTTP 500, 502, 503, 504.
- **Non-Retryable (Immediate Failure)**: HTTP 400 (Bad Request), HTTP 401 (Unauthorized), HTTP 403 (Forbidden), HTTP 404 (Not Found).

### Dead-Letter Queue Operations
Deliveries that exceed maximum attempts or experience fatal configuration issues are routed to `/settings/integrations/dead-letter`. Administrators can:
1. Inspect the full serialized JSON envelope and error message.
2. Click **[Retry]** to spawn a clean retry attempt without overwriting historical audit attempts.
3. Click **[Ignore]** to acknowledge and archive the dead-letter record.

---

## 8. External API Platform, Keys & Scopes

### Key Generation & Hashing
- Format: `rf_live_<32-character secure random token>`.
- Returned **strictly once** upon creation in the UI modal.
- Backend stores only:
  - `keyPrefix`: First 12 characters (e.g., `rf_live_a1b2...`) for identification.
  - `keyHash`: Cryptographic `SHA-256(rawApiKey)` for constant-time lookup and verification.

### Access Scopes Matrix
| Scope | Endpoints / Permitted Actions |
| :--- | :--- |
| `customers:read` | `GET /api/v1/external/customers`, `GET /api/v1/external/customers/{id}` |
| `customers:write` | `POST /api/v1/external/customers` |
| `products:read` | `GET /api/v1/external/products`, `GET /api/v1/external/products/{id}` |
| `inventory:read` | `GET /api/v1/external/inventory/availability` |
| `quotes:read` | `GET /api/v1/external/quotes` |
| `quotes:write` | `POST /api/v1/external/quote-requests` |
| `bookings:read` | `GET /api/v1/external/bookings`, `GET /api/v1/external/bookings/{id}` |
| `invoices:read` | `GET /api/v1/external/invoices`, `GET /api/v1/external/invoices/{id}` |
| `payments:read` | `GET /api/v1/external/payments` |
| `analytics:read` | `GET /api/v1/external/analytics/overview` |

---

## 9. Versioned External REST API (`/api/v1/external/...`)

### Standardized Error Format
```json
{
  "code": "FORBIDDEN",
  "message": "API key lacks required scope: [customers:write]",
  "requestId": "req_019543ab-8899-7711",
  "timestamp": "2026-09-01T22:35:00Z"
}
```

### Endpoints
- `GET /api/v1/external/customers` — List customers (tenant-scoped, PII redacted).
- `GET /api/v1/external/customers/{id}` — Customer details.
- `POST /api/v1/external/customers` — Create external customer / lead.
- `GET /api/v1/external/products` — List active rental products with public pricing.
- `GET /api/v1/external/products/{id}` — Product details with specs and dimensions.
- `GET /api/v1/external/inventory/availability` — Date-range stock availability check.
- `GET /api/v1/external/bookings` — Tenant rental bookings summary.
- `GET /api/v1/external/bookings/{id}` — Booking detail with line items.
- `GET /api/v1/external/invoices` — Issued invoice summaries.
- `POST /api/v1/external/quote-requests` — Inbound rental quote request.

---

## 10. Inbound Webhook Foundation & Deduplication

- Route: `POST /api/integrations/inbound/{provider}`
- Deduplication Key: `(provider, external_event_id, tenant_id)` with database unique index.
- If duplicate inbound event arrives, the system logs the occurrence as `IGNORED` and returns HTTP 200 OK without triggering redundant domain mutations.

---

## 11. Mock Accounting & CRM Reference Implementations

1. **Mock Accounting Connector (QuickBooks Demo)**:
   - Synchronizes invoices to external reference IDs (e.g., `MOCK-QB-INV-100234`).
   - Automatically maintains `ExternalEntityMapping` records linking `RentFlow ID` to `QuickBooks ID`.
2. **Mock CRM Connector (HubSpot Demo)**:
   - Synchronizes customers and approved quotes to external CRM contacts and deals (`MOCK-HS-DEAL-88392`).
3. **E-Commerce / Shopify Preparation**:
   - Outlines future bidirectional sync mapping where RentFlow's date-aware availability engine drives Shopify rental inventory availability.
4. **Zapier / Make / n8n Automation Support**:
   - Direct compatibility with low-code automation tools via standard generic webhooks with HMAC headers.

---

## 12. Security, Credential Masking & RBAC

- **Credential Masking**: All stored API secrets, webhook signing keys, and OAuth references are masked in UI representations (`••••••••5a2f`).
- **RBAC Matrix**:
  - `OWNER`, `ADMIN`: Full management of integrations, webhooks, dead letters, and API keys.
  - `FINANCE`: View and sync accounting connectors, read invoice events.
  - `SALES`: View CRM sync status, read quote/customer events.
  - `OPERATIONS_MANAGER`: View operational integration events.
  - `WAREHOUSE`, `DRIVER`, `CUSTOMER`: Access strictly blocked.
- **Tenant Isolation**: Every database query, repository lookup, and external API call filters explicitly by `tenantId`.

---

## 13. REST APIs Summary

### Integration Management
- `GET /api/integrations` — List active and available connectors.
- `POST /api/integrations` — Connect or configure a provider.
- `GET /api/integrations/{id}` — Get connection details and sync status.
- `POST /api/integrations/{id}/test` — Test third-party connection.
- `POST /api/integrations/{id}/sync` — Trigger manual data synchronization.
- `POST /api/integrations/{id}/disconnect` — Disconnect provider.
- `GET /api/integrations/{id}/sync-jobs` — List historical sync jobs.
- `GET /api/integrations/events` — Query tenant integration event audit log.
- `GET /api/integrations/dead-letter` — Query dead-lettered events.
- `POST /api/integrations/events/{id}/retry` — Manually retry failed event.

### Webhook Management
- `GET /api/integrations/webhooks` — List webhook endpoints.
- `POST /api/integrations/webhooks` — Create webhook endpoint (returns secret once).
- `GET /api/integrations/webhooks/{id}` — Webhook endpoint details.
- `PATCH /api/integrations/webhooks/{id}` — Update webhook endpoint.
- `DELETE /api/integrations/webhooks/{id}` — Delete webhook endpoint.
- `POST /api/integrations/webhooks/{id}/test` — Dispatch `webhook.test` event.
- `POST /api/integrations/webhooks/{id}/rotate-secret` — Rotate signing secret.
- `GET /api/integrations/webhooks/{id}/deliveries` — List delivery logs.
- `POST /api/integrations/webhook-deliveries/{id}/retry` — Manually retry delivery.

### External API Keys
- `GET /api/api-keys` — List active and revoked API keys.
- `POST /api/api-keys` — Generate new API key with scopes (returns token once).
- `POST /api/api-keys/{id}/revoke` — Revoke API key.

---

## 14. Angular Screens

1. `/settings/integrations`: Dashboard with summary KPI cards, connector catalog, quick status indicators, and sync stats.
2. `/settings/integrations/:id`: Detailed connector view with capability badges, connection health, "Sync Now" / "Test Connection", and sync history.
3. `/settings/integrations/webhooks`: Webhook endpoints management, status toggles, secret rotation, and test trigger.
4. `/settings/integrations/webhooks/:id`: Endpoint detail, delivery history log with HTTP status badges, duration, and payload viewer modal.
5. `/settings/integrations/events`: Tenant-wide event audit stream with payload JSON viewer.
6. `/settings/integrations/dead-letter`: Dead-letter management with failure reasons and manual retry actions.
7. `/settings/api-keys`: API keys management with granular scope pills and one-time key generator modal.
