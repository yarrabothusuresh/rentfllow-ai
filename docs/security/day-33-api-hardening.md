# Day 33 Security Architecture: API Attack-Surface Hardening

## 1. Executive Summary & Core Security Principles

Day 33 hardens the external API attack surface of RentFlow AI. Having established authentication (Day 31) and tenant/customer data isolation (Day 32), Day 33 focuses on protecting the HTTP boundary against:
- Cross-origin abuse and unauthorized API consumers
- Brute-force credential attacks and login flooding
- High-volume DoS and AI compute scraping attacks
- Memory exhaustion via oversized payloads and uncontrolled query lengths
- Browser-side vulnerabilities (MIME sniffing, clickjacking, unsafe framing, cross-origin referrer leaks)
- Mass-assignment and privilege escalation via untrusted DTO binding
- Information leakage through unhandled exceptions, raw SQL traces, and internal stack dumps

### Golden Principles of Day 33 Hardening:
1. **Zero Wildcard CORS**: No `allowedOriginPatterns("*")` or wildcard origins. Allowed origins are strictly centralized and configurable.
2. **Deterministic Rate Limiting**: Every incoming request category has an explicit request-per-minute (RPM) quota enforced before expensive computation or database interaction.
3. **Defense-in-Depth Security Headers**: Every HTTP response carries explicit defensive browser instructions (`nosniff`, `frame-ancestors 'self'`, `strict-origin-when-cross-origin`, restrictive `Permissions-Policy`, and Angular-safe `Content-Security-Policy`).
4. **Authoritative Identity & Strict DTO Validation**: Never trust incoming payload fields for `tenantId`, `role`, `status`, or pricing calculations. Reject malformed inputs with `@Valid` before controller execution.
5. **Sanitized Error Responses**: Internal exception details, database dialect traces, and stack traces are suppressed from production responses; clients receive consistent, structured error payloads.

---

## 2. Threat Model & Attack Scenarios

### Attack Scenario 1: Cross-Origin Resource Theft & Preflight Spoofing
- **Vector**: Malicious third-party websites (`https://evil-hacker.com`) issue credentialed XMLHttpRequests / fetch calls to RentFlow backend endpoints.
- **Vulnerability**: Previously, 27 controllers had `@CrossOrigin(origins = "*")` and `WebConfig` configured wildcard origin patterns.
- **Impact**: Cross-origin leakage of sensitive rental records, customer PII, and quote details.
- **Mitigation**:
  - Removed all 27 `@CrossOrigin` annotations from all controllers across the backend.
  - Eliminated conflicting `WebConfig.addCorsMappings`.
  - Established a single centralized `CorsConfigurationSource` in `SecurityConfig` with explicit allowed origins (`http://localhost:4200`, `http://localhost:8080`, and configurable `rentflow.security.cors.allowed-origins`), explicit HTTP methods (`GET`, `POST`, `PUT`, `DELETE`, `PATCH`, `OPTIONS`), explicit allowed headers, credentials enabled, and 1-hour preflight caching (`maxAge = 3600`).

### Attack Scenario 2: Brute-Force & Credential Stuffing on Login Endpoints
- **Vector**: Automated botnets send thousands of credential guesses against `/api/auth/login` and `/api/portal/auth/login`.
- **Vulnerability**: Login endpoints previously allowed unlimited attempts, allowing password guessing and thread pool starvation.
- **Mitigation**:
  - Integrated `RateLimitingService` directly into `AuthenticationService.login()` and portal authentication.
  - Enforced a strict quota of 10 requests per minute per client IP for `AUTH_LOGIN`.
  - Added safe client IP resolution (`resolveSafeClientIp`) inspecting sanitized forward headers and validating against IPv4/IPv6 regex to prevent header spoofing.
  - Excess attempts immediately return HTTP **429 Too Many Requests** with `Retry-After` metadata.

### Attack Scenario 3: AI Tool & Copilot Scraping / Denial of Wallet
- **Vector**: Callers send massive volumes of heavy prompts to `/api/ai/copilot/chat` and `/api/ai/sales-agent`, exhausting server CPU and third-party LLM API quotas.
- **Vulnerability**: AI endpoints had no rate limits, accepted unbounded message sizes, and `CopilotController` had a dangerous fallback reading `X-User-Role: OWNER` directly from headers.
- **Mitigation**:
  - Enforced `RateLimitCategory.AI_COPILOT` (20 RPM) and `RateLimitCategory.AI_SALES` (20 RPM) per tenant/user.
  - Restricted `CopilotChatRequestDTO` and `AIRequest` prompt inputs to `@Size(max = 4000)`.
  - Removed all `X-User-Role` header fallbacks in `CopilotController`; role and tenant are strictly extracted from the verified `SecurityContext`.

### Attack Scenario 4: Request Buffer & Memory Exhaustion (Oversized Payloads)
- **Vector**: Attackers send multi-megabyte JSON strings or massive multipart uploads to exhaust JVM heap.
- **Vulnerability**: Default Spring Boot servlet file upload limits (typically 128MB+ or unbounded swallow sizes).
- **Mitigation**:
  - Hardened `application.properties`:
    - `spring.servlet.multipart.max-file-size=10MB`
    - `spring.servlet.multipart.max-request-size=10MB`
    - `server.tomcat.max-swallow-size=10MB`
  - Added `@Size` constraints to free-text fields (`notes`, `description`, `search`, etc.) preventing massive text ingestion.
  - Clamped all search query parameters (`search`, `q`) to 200 characters max.

### Attack Scenario 5: Missing Defensive Security Headers
- **Vector**: Attackers frame RentFlow in an `<iframe>` on a malicious domain (Clickjacking), rely on browser MIME-type sniffing to execute polyglot files, or leak sensitive URL tokens via the `Referer` header.
- **Vulnerability**: Missing explicit security headers on API responses.
- **Mitigation**: Configured in `SecurityConfig.securityFilterChain`:
  - `X-Content-Type-Options: nosniff` (prevents MIME type sniffing)
  - `X-Frame-Options: SAMEORIGIN` and `Content-Security-Policy: frame-ancestors 'self'` (prevents framing/clickjacking)
  - `Referrer-Policy: strict-origin-when-cross-origin` (prevents path leakage across origin boundaries)
  - `Permissions-Policy: camera=(), microphone=(), geolocation=()` (disables unused browser hardware APIs)
  - `Content-Security-Policy: default-src 'self'; script-src 'self' 'unsafe-inline'; ...` (Angular-safe baseline CSP)

### Attack Scenario 6: Mass-Assignment & Untrusted Field Binding
- **Vector**: Clients submit JSON bodies containing `role: "OWNER"`, `tenantId: "victim-tenant"`, `status: "APPROVED"`, or `subtotal: 0.01` when creating/updating resources.
- **Vulnerability**: Controller blindly deserializing client JSON into entity objects or accepting caller-provided financial totals.
- **Mitigation**:
  - Strict DTO separation: client payloads bind to dedicated DTOs with Jakarta Validation (`@NotNull`, `@NotBlank`, `@Size`, `@Email`).
  - Server-authoritative calculations: `QuoteCalculationService` computes all subtotals, taxes, deposits, and balances from catalog item rates; client-supplied price fields are overwritten.
  - Server-authoritative identity: `tenantId` is always injected from `CurrentUserService.requireTenantId()`; incoming client `tenantId` fields are ignored or validated.

### Attack Scenario 7: Sensitive Information Leakage in Error Responses
- **Vector**: Fuzzing endpoints with malformed JSON, SQL injection probes, or type mismatch inputs to trigger 500 errors and read database stack traces.
- **Vulnerability**: Unhandled exceptions returning raw Java stack traces and Hibernate SQL dialects.
- **Mitigation**:
  - Created `GlobalExceptionHandler` annotated with `@RestControllerAdvice`.
  - Normalized structured response format:
    ```json
    {
      "timestamp": "2026-09-14T14:20:00Z",
      "status": 400,
      "error": "Bad Request",
      "message": "Validation failed for request.",
      "path": "/api/customers",
      "validationErrors": {
        "email": "Invalid email address format."
      }
    }
    ```
  - Sanitized generic 500 errors to `"An internal server error occurred. Please contact support."` while logging root causes securely on the server.

---

## 3. Rate Limiting Multi-Category Architecture

The `RateLimitingService` implements an in-memory Token Bucket algorithm with periodic background eviction of stale buckets. Quotas are categorized as follows:

| Category | Description | Default Quota (RPM) | Key Derivation |
|---|---|---|---|
| `AUTH_LOGIN` | Login & authentication attempts | 10 | `auth_login:<sanitized-ip>` |
| `PUBLIC_STOREFRONT` | Catalog browsing & search | 60 | `storefront:<sanitized-ip>` |
| `PUBLIC_CHECKOUT` | Public booking submissions | 15 | `checkout:<sanitized-ip>` |
| `AI_SALES` | AI Sales Agent inquiries | 20 | `ai_sales:<tenantId>:<user/ip>` |
| `AI_COPILOT` | Copilot chat assistant | 20 | `ai_copilot:<tenantId>:<user/ip>` |
| `PHONE_AI` | Voice & IVR webhooks | 30 | `phone_ai:<sanitized-ip>` |
| `WEBHOOK` | External webhook callbacks | 120 | `webhook:<sanitized-ip>` |
| `EXTERNAL_API` | General external API calls | 100 | `ext_api:<tenantId/ip>` |

### Safe Client IP Resolution
To protect against `X-Forwarded-For` header spoofing:
1. Header values are inspected sequentially: `X-Forwarded-For`, `X-Real-IP`, `Proxy-Client-IP`.
2. The first non-empty IP entry is extracted and sanitized.
3. The resolved IP is validated against standard IPv4 (`^([0-9]{1,3}\.){3}[0-9]{1,3}$`) and IPv6 patterns. If malformed or absent, it falls back to `request.getRemoteAddr()`.

---

## 4. Frontend Resilience

In `frontend/src/app/services/auth.interceptor.ts`:
- Intercepts HTTP status `429 Too Many Requests`.
- Logs a user-friendly warning: `"Rate limit exceeded: Too many requests. Please wait and try again shortly."`
- Prevents cascading request storms and supplies consistent UI feedback without breaking user state.

---

## 5. Multi-Instance & Distributed Scalability Note

> [!IMPORTANT]
> **In-Memory Rate Limiting Limitation**:
> The `RateLimitingService` uses JVM-local concurrent maps (`ConcurrentHashMap`). In a horizontally scaled environment with multiple backend instances behind a load balancer, rate limits are enforced on a per-node basis unless sticky sessions are configured.
> 
> **Roadmap**: Distributed rate limiting backed by **Redis** is scheduled for **Day 41** as part of infrastructure clustering. For Day 33, in-memory rate limiting provides robust single-node protection without adding external operational dependencies.
