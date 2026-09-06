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

## 5. Vulnerability Reporting
For security concerns or vulnerability disclosures, contact security@rentflow.ai.
