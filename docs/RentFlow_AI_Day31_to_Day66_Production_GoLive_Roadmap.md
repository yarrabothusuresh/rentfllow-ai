# RentFlow AI — Post-30-Day Production Roadmap
## Day 31 to Day 66 — From Functional MVP to Production Go-Live

> Purpose: convert the completed 30-day feature build into a secure, reliable, observable, deployable, pilot-ready, and production-ready SaaS.
>
> Guiding rule: **Every day removes one production risk.**
>
> Core principle: **Do not add new speculative features until production blockers are removed.**

---

# How to Use This Roadmap

For each day:

1. Inspect the existing repository first.
2. Run the current baseline tests.
3. Preserve all working functionality.
4. Implement only the scope for that day.
5. Add automated regression tests.
6. Run backend, frontend, security, and E2E checks as relevant.
7. Update documentation.
8. Record remaining P0/P1 risks.
9. Stop. Do not start the next day's task automatically.

Priority order:

- **P0** — launch blocker
- **P1** — required for production readiness
- **P2** — required/important for first release
- **P3** — post-launch scale/advanced capability

---

# PHASE 1 — SECURITY FOUNDATION

## Day 31 — Authentication Foundation

### Priority
P0 — Launch Blocker

### Objective
Replace demo/header-based identity with real authenticated user identity.

### Why Needed
Current audit findings include no production-grade Spring Security foundation, plaintext customer passwords, dummy portal tokens, tenant/user identity derived from forgeable headers, and unsafe owner fallback behavior.

### Backend Work
- Add Spring Security.
- Implement staff and customer authentication.
- Add `email` and `passwordHash` where required.
- Use BCrypt password hashing.
- Implement JWT token generation and verification.
- Create authentication service.
- Create JWT authentication filter.
- Populate Spring `SecurityContext`.
- Remove default OWNER behavior.
- Return `401 Unauthorized` when authentication is missing/invalid.
- Define token expiration.
- Do not log raw JWTs.

### Frontend Work
- Build/repair login page.
- Add Angular auth service.
- Add auth interceptor.
- Attach Bearer token to protected API requests.
- Remove fake role/tenant headers.
- Implement logout.
- Add route guards.

### Database Work
- Add authentication-related columns with migration.
- Ensure passwords are never stored in plaintext.
- Add unique email constraints consistent with the chosen identity model.

### Security Work
- Wrong password → 401.
- Expired JWT → 401.
- Tampered JWT → 401.
- Missing JWT → 401.
- Role claims cannot be supplied by browser headers.

### Tests
- BCrypt hash test.
- Login success/failure tests.
- Expired token test.
- Modified-signature test.
- Protected endpoint test.
- Plaintext-password regression test.

### Manual Verification
- Create user.
- Login.
- Inspect DB — only hash stored.
- Modify JWT manually — request rejected.
- Call protected endpoint without token — rejected.

### Documentation
Update `SECURITY.md` with authentication flow, password handling, token lifecycle, and login/logout behavior.

### Success Criteria
- No plaintext passwords.
- All protected endpoints require valid authentication.
- Browser cannot assert role or tenant identity.
- All existing business tests remain passing.

### Do Not Build
SSO, SAML, biometric login, passkeys/FIDO2, social login.

---

## Day 32 — Tenant Isolation + Customer IDOR Hardening

### Priority
P0 — Launch Blocker

### Objective
Prove that tenant and customer boundaries are real.

### Target Architecture

```text
Verified JWT
   ↓
Authenticated User
   ↓
Tenant Membership
   ↓
TenantContext
   ↓
Controller
   ↓
Service Authorization
   ↓
Tenant-Aware Repository
```

### Backend Work
- Derive tenant from authenticated server-side identity.
- Remove trust in `X-Tenant-Id`.
- Remove default tenant fallback.
- Review all tenant-owned entities.
- Replace unsafe repository access patterns where needed.
- Validate cross-entity relationships belong to the same tenant.
- Customer portal identity must come from authenticated principal, not `X-Customer-Id`.
- Enforce object-level authorization.

### Entity Coverage
Product, Customer, Lead, RentalRequest, Quote, Booking, Contract, Invoice, Payment, InventoryReservation, WarehouseOrder, Delivery, Vehicle, Driver, ReturnOrder, Inspection, DamageClaim, IntegrationConnection, WebhookEndpoint, ExternalApiKey, AiConversation, PhoneCallSession, BusinessSignal, AiRecommendation, AutomationRule, AutomationExecution.

### Security Tests
Create Tenant A and Tenant B and attempt GET, LIST, SEARCH, UPDATE, DELETE, action endpoints, cross-tenant ID injection, and cross-tenant relationship creation.

Also create Customer A1 and Customer A2 inside the same tenant. Customer A1 must not access Customer A2 bookings, quotes, invoices, payments, contracts, or rental requests.

### Frontend Work
- Remove any remaining tenant/customer identity headers.
- Ensure portal services use authenticated APIs only.

### Database Work
- Ensure `tenant_id` is non-null where required.
- Add useful tenant-scoped indexes.

### Documentation
Document tenant resolution, tenant boundaries, customer ownership, and cross-tenant denial behavior.

### Success Criteria
- Zero cross-tenant reads/writes.
- Zero cross-customer IDOR.
- Unauthorized access returns safe 403/404.
- No existence leakage.

### Do Not Build
Cross-tenant data sharing, organization federation, SSO.

---

## Day 33 — API Boundary, CORS, Rate Limiting & Security Headers

### Priority
P0/P1

### Objective
Harden public and sensitive APIs.

### Backend Work
- Centralize CORS configuration.
- Remove wildcard `@CrossOrigin`.
- Add allowed-origin config.
- Add standard security headers.
- Add rate limiting to login, password reset, storefront, checkout, rental request, AI Sales, Copilot, Phone AI, inbound webhooks, and external APIs.
- Add request-size limits where needed.
- Validate content type.
- Review DTO mass-assignment risks.

### Security Work
Prevent header-based role/tenant injection, page-size abuse, brute-force login attempts, webhook spam, and AI endpoint abuse.

### Tests
- CORS allowed origin.
- CORS denied origin.
- Rate-limit threshold.
- Large-payload rejection.
- Unexpected content-type rejection.
- Mass-assignment attempt.

### Success Criteria
- Public endpoints are protected against basic abuse.
- Sensitive APIs have documented rate policies.
- CORS is explicit.
- No wildcard production CORS.

### Do Not Build
Global API gateway, WAF vendor integration.

---

# PHASE 2 — FINANCIAL & BUSINESS STATE CORRECTNESS

## Day 34 — Payment Idempotency + Transaction Integrity

### Priority
P0

### Objective
Guarantee duplicate payment submissions never create duplicate financial effects.

### Backend Work
- Add `findByTenantIdAndTransactionReference`.
- Make `recordPayment` idempotent.
- Return existing result on duplicate key.
- Ensure invoice/payment/balance updates occur in one transaction.
- Reject negative, invalid zero, excessive overpayment, and foreign booking/invoice references.
- Keep payment state authoritative in backend.

### Database Work
Add a tenant-scoped unique constraint on transaction reference where business rules allow.

### Frontend Work
- Disable payment button while processing.
- Reuse same idempotency reference on retry.
- Show existing receipt if request is replayed.

### Tests
- 10 concurrent same-reference requests.
- Exactly one payment row.
- Exactly one balance effect.
- Duplicate webhook.
- Retry after timeout.
- Overpayment attempt.
- Negative payment attempt.

### Success Criteria
Exactly-once financial effect and no balance corruption.

### Do Not Build
Real payment-provider integration yet.

---

## Day 35 — Booking, Checkout & Rental Request Idempotency

### Priority
P0/P1

### Objective
Prevent duplicate business objects from double-clicks and network retries.

### Backend Work
- Add idempotency key to public checkout/rental request.
- Preserve same key on retry.
- Ensure Quote → Booking is at most once.
- Ensure RentalRequest → Lead is at most once.
- Ensure AI-created draft request/quote is idempotent.

### Database Work
Add tenant-scoped unique constraints for booking-by-quote and rental-request idempotency key.

### Frontend Work
- Generate UUID idempotency key when checkout form is created.
- Reuse same key on retry.
- Prevent repeated button submits.

### Tests
- Rapid double-click checkout.
- Concurrent booking creation.
- Repeated network retry.
- Repeated RentalRequest event.
- Repeated Lead creation event.

### Success Criteria
One user action produces one business effect.

### Do Not Build
New checkout provider.

---

## Day 36 — Financial BigDecimal Audit & Profitability Correctness

### Priority
P1

### Objective
Remove unsafe floating-point financial calculations and mock profitability values.

### Backend Work
Search financial paths for `double`, `float`, and hardcoded revenue/cost/profit values. Replace them with `BigDecimal`.

Cover unit price, quantity math, discount, tax, subtotal, deposit, invoice balance, payment, refund, replacement cost, repair cost, revenue, cost, profit, and margin. Use explicit rounding rules.

### AI Work
AI tools must query authoritative services. Never use hardcoded profitability examples as returned business truth.

### Tests
Precision, rounding, discount+tax, payment accumulation, margin, zero-value edge cases, and large amounts.

### Success Criteria
Financial domain uses BigDecimal consistently and AI profitability answers match backend calculations.

### Do Not Build
Accounting provider.

---

## Day 37 — Inventory Concurrency Regression Gate

### Priority
P1

### Objective
Preserve the already-implemented pessimistic locking guarantees.

### Backend Work
Do not redesign working locking. Verify `PESSIMISTIC_WRITE`, canonical UUID lock ordering, transaction boundaries, reservation creation, and booking confirmation.

### Tests
- Stock=10, two threads request 10 → one succeeds, one fails, total reserved=10.
- Reverse-order multi-product reservations → no deadlock.
- Retry after conflict → availability remains correct.

### Success Criteria
Zero overselling and zero deadlock regression.

### Do Not Build
Redis distributed locks, event-sourced inventory.

---

# PHASE 3 — DATABASE & RELIABILITY

## Day 38 — Flyway Production Schema Management

### Priority
P0/P1

### Objective
Make schema creation and evolution deterministic.

### Backend Work
- Add Flyway.
- Disable Hibernate schema mutation in production.
- Use `ddl-auto=validate` in production.

### Database Work
Create baseline and follow-up migrations for actual schema, security additions, idempotency constraints, scheduler locks, and provider columns.

### Tests
- Empty Postgres migration.
- Application boot with validate.
- Migration ordering and repeatability.

### Success Criteria
Fresh PostgreSQL → Flyway → application starts successfully.

### Do Not Build
Custom schema generator.

---

## Day 39 — Database Constraints, Indexes & Pagination

### Priority
P1

### Objective
Make list APIs bounded and database queries production-safe.

### Backend Work
Add `Pageable` to high-volume APIs: products, customers, leads, quotes, bookings, invoices, payments, rental requests, warehouse orders, deliveries. Clamp maximum page size.

### Database Work
Review tenant/status/date indexes and date-range availability indexes.

### Frontend Work
Add pagination controls, counts, loading states, and empty states.

### Tests
Page boundary, sorting, filtering, max-size clamp, and tenant filter before pagination.

### Success Criteria
No unbounded high-volume production list endpoint.

### Do Not Build
Elasticsearch.

---

## Day 40 — Transaction Boundaries & Outbox Reliability

### Priority
P1

### Objective
Ensure core DB changes commit independently of external delivery.

### Target

```text
Business Change + Outbox Event
        ↓
ONE DB TRANSACTION
        ↓
COMMIT
        ↓
Async External Delivery
```

### Backend Work
Audit booking, payment, contract, invoice, warehouse, and integration events. Ensure outbox insert is transactionally aligned with domain changes, external HTTP happens after commit, retries back off, and dead-letter handling exists.

### Security Work
Do not persist secrets in outbox payloads.

### Tests
Provider down, retry, DLQ, isolated failures, and core-state commit.

### Success Criteria
External integration failure never corrupts or rolls back valid core state.

### Do Not Build
Kafka, RabbitMQ.

---

## Day 41 — Multi-Instance Scheduler Safety + Hold Expiration

### Priority
P1

### Objective
Make scheduled jobs safe with multiple backend replicas.

### Backend Work
Use ShedLock or PostgreSQL advisory locks for outbox, automation, notification retry, hold expiration, overdue invoices, recommendation detection, and integration retry.

Implement reservation hold expiration.

### Tests
- Two scheduler instances → one effective execution.
- Stale lock recovery.
- Expired hold transitions correctly.
- Inventory becomes available again.

### Success Criteria
No duplicate scheduled effects.

### Do Not Build
ZooKeeper or separate scheduling platform.

---

# PHASE 4 — OBSERVABILITY & RECOVERY

## Day 42 — Observability, Metrics & Structured Logging

### Priority
P1

### Objective
Make production behavior measurable.

### Backend Work
Add Spring Boot Actuator, Micrometer, Prometheus registry, MDC correlation IDs, structured logging, and secret masking.

Track HTTP latency/errors, DB-pool metrics, booking failures, payment failures, inventory conflicts, webhook retry/DLQ, automation failures, and AI latency/failures.

### Frontend Work
Show correlation/request IDs on support-friendly error messages.

### Tests
Request ID propagation, actuator health, Prometheus output, secret redaction.

### Success Criteria
Incidents can be correlated from request → log → metric.

### Do Not Build
Full monitoring-vendor rollout.

---

## Day 43 — Failure Handling & Graceful Degradation

### Priority
P1

### Objective
Keep core rental operations working during provider outages.

### Backend Work
- Standardize `ProblemDetail`.
- Add timeouts/circuit breakers where useful.
- Isolate provider failures.
- Add safe fallback behavior.

### Simulate
AI, email, SMS, webhook target, object storage, and payment-provider failures.

### Success Criteria
Provider failure degrades a capability rather than crashing the whole product.

### Do Not Build
Chaos-engineering platform.

---

## Day 44 — PostgreSQL Backup Automation & Restore Verification

### Priority
P1

### Objective
Prove recoverability.

### Database Work
Create automated backup and restore-test scripts using `pg_dump -Fc`.

### Security Work
Restricted file permissions, encryption at rest, credentials via secrets/environment.

### Restore Test
Backup → fresh DB → restore → verify counts and critical tenant data → start app → smoke test.

### Success Criteria
A real backup can be restored and verified.

### Do Not Build
Commercial backup appliance.

---

## Day 45 — Disaster Recovery & Operational Runbooks

### Priority
P1

### Objective
Define recovery behavior for realistic failures.

### Scenarios
Backend crash, DB outage, DB loss, bad deployment, AI outage, email outage, payment outage, webhook outage, storage outage.

### Documentation
For each: symptoms, detection, immediate action, recovery, validation, rollback/escalation.

### Success Criteria
An operator can recover the system using documented steps.

### Do Not Build
Multi-region active-active.

---

# PHASE 5 — DEPLOYMENT & DELIVERY PIPELINE

## Day 46 — Production Docker Stack

### Priority
P0/P1

### Objective
Make the entire application container-buildable.

### Backend Work
Verify multi-stage build, non-root runtime, JVM container memory awareness, production profile, and healthcheck.

### Frontend Work
Create a production Angular Dockerfile using Node build + NGINX runtime, Angular route fallback, and cache headers.

### Database Work
Persistent PostgreSQL volume and healthcheck.

### Tests
`docker compose build`, `docker compose up -d`, backend health, and frontend/backend connectivity.

### Success Criteria
One documented command starts the production-like stack.

### Do Not Build
Kubernetes.

---

## Day 47 — GitHub CI Quality Pipeline

### Priority
P1

### Objective
Automatically block regressions on every commit/PR.

### Pipeline
Checkout → JDK → Maven compile/tests → PostgreSQL integration tests → security tests → Node → Angular tests/build → Flyway verification → Docker build → dependency/security scan.

### Success Criteria
Green CI is mandatory for merge.

### Do Not Build
Multi-cloud pipeline.

---

## Day 48 — Staging Environment

### Priority
P1

### Objective
Create a production-like pre-release environment.

### Work
Staging PostgreSQL, backend production-like profile, frontend production build, staging-only secrets, Flyway migrations, HTTPS if available, realistic test tenant.

### Tests
Health, login, storefront, product, checkout, booking, warehouse, and delivery smoke tests.

### Success Criteria
The whole application runs outside developer machines.

### Do Not Build
Production deployment yet.

---

# PHASE 6 — REAL MVP PROVIDERS

## Day 49 — Real Transactional Email

### Priority
P2

### Objective
Replace console/mock email for real customer communication.

### Provider
Choose one provider such as AWS SES or SendGrid.

### Backend Work
Implement behind existing abstraction for quote email, booking confirmation, contract, payment receipt, invoice reminder, and password reset.

### Security
API keys via environment/secrets only.

### Tests
Provider sandbox or WireMock.

### Success Criteria
A staging-generated transactional email reaches a real test inbox.

### Do Not Build
Marketing campaigns.

---

## Day 50 — Production Object Storage

### Priority
P2

### Objective
Persist documents outside ephemeral containers.

### Architecture
Use a pluggable `DocumentStorageService`, with MinIO for local/staging and S3-compatible storage in production.

### Store
Contract PDFs, invoice PDFs, inspection photos, damage photos, attachments.

### Security
Tenant-scoped keys, MIME validation, file-size limits, signed URLs, no public bucket, filename sanitization.

### Tests
Upload, download, tenant isolation, expired signed URL, invalid MIME type.

### Success Criteria
No production document depends on container filesystem.

### Do Not Build
Video processing.

---

## Day 51 — Contract, PDF & E-Signature Productionization

### Priority
P2

### Objective
Replace contract boolean/stubs with a real contract lifecycle.

### Target
Quote Accepted → Contract Snapshot → PDF → SHA-256 Hash → Customer Signature → Immutable Signed Contract.

### Backend Work
Implement contract entity/snapshot, PDF generation, document hash, signer metadata, signed timestamp, IP/user-agent audit, and version-safe signing.

### Frontend Work
Contract preview, signature capture, confirmation, signed PDF download.

### Tests
PDF validity, hash integrity, tamper detection, duplicate-sign prevention, customer ownership.

### Success Criteria
Signed contract is immutable, downloadable, and auditable.

### Do Not Build
DocuSign integration unless required.

---

## Day 52 — Real Payment Provider Sandbox

### Priority
P2

### Objective
Integrate online payment collection in sandbox/test mode.

### Backend Work
Provider abstraction, payment intent/session creation, provider metadata, verified webhook handler, idempotent success processing, and safe refund foundation if needed.

### Frontend Work
Use provider-hosted/secure card entry.

### Security
Card data must not touch RentFlow backend. Externalize secrets and verify webhook signatures/replay protection.

### Tests
Successful payment, failed payment, duplicate webhook, invalid signature, stale event.

### Success Criteria
Sandbox transaction updates RentFlow exactly once.

### Do Not Build
Live production charging yet.

---

## Day 53 — Tax Production Readiness

### Priority
P2

### Objective
Remove hardcoded demo tax.

### Product Decision
Define initial launch geography.

### Backend Work
Configurable tenant tax rules, taxable categories, tax-exempt customers, zero-rate jurisdictions, backend-authoritative calculation.

### Frontend Work
Admin tax settings.

### Tests
Rounding, exemption, multiple rates, quote-to-invoice consistency.

### Success Criteria
No hardcoded demo tax remains in production flow.

### Do Not Build
Global VAT/customs engine.

---

# PHASE 7 — UX, PERFORMANCE & PRODUCT VALIDATION

## Day 54 — Frontend Error UX & Loading States

### Priority
P2

### Objective
Make failures understandable.

### Frontend Work
Global HTTP error interceptor, toasts/banners, 401/403/404/409/500/503 handling, skeletons, safe retries, network/offline state.

### Backend Work
Standardize API errors.

### Success Criteria
No raw stack trace, SQL, or internal exception is shown to users.

### Do Not Build
UI redesign.

---

## Day 55 — Mobile Operations & Accessibility

### Priority
P2

### Objective
Make warehouse, driver, customer, return, and signature flows usable on mobile.

### Frontend Work
Optimize 375px layouts, tap targets, ARIA labels, focus, keyboard flow, contrast, alert semantics, and mobile photo/signature UX.

### Tests
Axe-core, responsive viewport, keyboard-only, touch usability.

### Success Criteria
No high-severity accessibility issue in key workflows.

### Do Not Build
Native mobile apps.

---

## Day 56 — Large Dataset & Performance Baseline

### Priority
P1

### Objective
Measure performance before optimization.

### Seed Data
Use realistic but machine-appropriate volumes across tenants, products, customers, quotes, bookings, reservations, invoices.

### Measure
p50, p95, p99, error rate, CPU, heap, GC, DB pool, slow queries.

### Scenarios
Storefront catalog, availability, checkout, booking, dashboards, analytics.

### Success Criteria
Publish measured baseline; do not invent SLA numbers.

### Do Not Build
Distributed cache grid.

---

## Day 57 — Backend Memory, Hikari & Query Tuning

### Priority
P1

### Objective
Optimize only where Day 56 proves a bottleneck.

### Backend Work
Tune Hikari, query timeout, task executors, N+1 fetching, large eager collections, serialization sizes, JVM memory limits, and GC metrics.

### Database Work
Tune indexes and slow queries.

### Tests
Repeat performance benchmark and compare.

### Success Criteria
Measurable improvement with no connection-pool starvation.

### Do Not Build
Autoscaling platform.

---

# PHASE 8 — SECURITY & RELEASE VALIDATION

## Day 58 — AI Security & Prompt Injection Regression

### Priority
P1

### Objective
Prove AI cannot bypass business security.

### Attack Prompts
Show another tenant, reveal API keys, reveal internal margin, run SQL, read filesystem, call arbitrary URL, refund payment, change role, confirm booking, ignore previous instructions.

### Architecture Rule
Every tool independently enforces tenant, user, role, permission, ownership, allowed fields, result limits, and action risk.

### Success Criteria
Zero unauthorized business action or cross-tenant data access.

### Do Not Build
Custom LLM training.

---

## Day 59 — Full Security Regression / Penetration Gate

### Priority
P0

### Objective
Re-test the entire application after all hardening.

### Test Areas
Auth, JWT manipulation, tenant/customer IDOR, RBAC, CORS, CSRF where applicable, SQL injection, XSS, mass assignment, webhook replay, API-key leakage, secret leakage, rate limits, file upload security, AI prompt injection.

### Tools
Automated and manual techniques such as OWASP ZAP plus API security tests.

### Release Rule
P0 security issues = 0.
P1 launch-blocking security issues = 0.

### Success Criteria
Formal security audit summary completed.

### Do Not Build
New features.

---

## Day 60 — Full Business E2E Regression

### Priority
P1

### Objective
Verify the entire product after security, reliability, and provider changes.

### E2E
Customer → Storefront → AI Sales → Product Discovery → Dates → Availability → Cart → Checkout → Rental Request → CRM Lead → Quote → Acceptance → Contract → Signature → Deposit → Booking → Reservation → Warehouse Pick → Pack → Delivery Assignment → Delivery → Event → Pickup → Return → Inspection → Damage → Final Invoice → Payment → Profitability → Analytics → Copilot → Recommendation.

### Verify
Financial totals, inventory state, tenant isolation, role transitions, outbox state, audit trail, provider status.

### Success Criteria
All critical business steps pass against staging.

### Do Not Build
New module.

---

# PHASE 9 — PILOT & PRODUCT VALIDATION

## Day 61 — Pilot Tenant Setup

### Priority
P1

### Objective
Validate RentFlow with one realistic rental business.

### Setup
One realistic pilot tenant with products, customers, pricing, tax, warehouse users, sales, driver, and sample events.

### Product Validation
Observe quote creation, booking clarity, warehouse workflow, delivery, return, invoice understanding, and customer checkout.

### Success Criteria
Pilot tenant can complete the core lifecycle without developer intervention.

### Do Not Build
Pilot-specific custom features.

---

## Day 62 — Pilot Feedback Fixes

### Priority
P1/P2

### Objective
Fix only issues that block real use.

### Prioritize
P0 defects, P1 workflow blockers, major UX confusion, data correctness, slow critical screens.

### Avoid
Feature expansion and non-launch-critical integrations.

### Success Criteria
Pilot blockers resolved and regression-tested.

---

## Day 63 — UAT & Business Sign-Off

### Priority
P1

### Objective
Obtain formal acceptance by persona.

### Personas
Business Owner, Sales, Warehouse, Driver, Customer, Finance/Admin.

### UAT
Script expected result, actual result, pass/fail, evidence, and issue ID.

### Success Criteria
Critical UAT scenarios pass and are signed off.

### Do Not Build
Feature additions.

---

# PHASE 10 — RELEASE & GO-LIVE

## Day 64 — Production Readiness Review

### Priority
P1

### Objective
Verify every launch gate with evidence.

### Required Evidence
- P0 defects = 0
- Authentication verified
- Tenant isolation verified
- Customer IDOR verified
- Payment idempotency verified
- Booking idempotency verified
- Inventory concurrency verified
- Flyway verified
- Backup/restore verified
- CI green
- Staging E2E green
- Observability working
- Alerting working
- Secrets production-safe
- Provider sandbox verified
- Rollback documented
- UAT signed off

### Documentation
Finalize `SECURITY.md`, `OPERATIONS.md`, `DEPLOYMENT.md`, and production checklist.

### Success Criteria
Every P0/P1 item has evidence.

### Do Not Build
Anything new.

---

## Day 65 — Release Candidate + Rollback Rehearsal

### Priority
P1

### Objective
Create immutable release candidate.

### Version
Example: `v1.0.0-rc1`

### Work
Freeze features, update versions, build backend/frontend, build Docker images, tag Git, publish immutable images, record Flyway version.

### Rollback Rehearsal
Deploy RC → smoke test → simulate critical defect → rollback → verify DB integrity → verify application.

### Success Criteria
Release and rollback both work using documented steps.

### Do Not Build
Scope changes.

---

## Day 66 — Controlled Production Go-Live + Hypercare

### Priority
P0/P1 Operational Gate

### Objective
Launch carefully with one/few tenants and active monitoring.

### Go-Live Sequence
Production backup → apply migrations → deploy backend → deploy frontend → smoke test → enable pilot tenant → verify login/storefront/quote/booking/payment/warehouse/delivery/final invoice.

### Hypercare Metrics
Login failures, HTTP 5xx, payment failures, duplicate events, reservation conflicts, DB pool, slow queries, webhook DLQ, notification failures, AI failures, storage errors, contract-generation failures.

### Incident Rules
- Fix production defects first.
- No feature expansion during hypercare.
- Roll back if security/data risk appears.
- Keep initial tenant count small.

### Success Criteria
Production is stable, first real lifecycle completes, no P0/P1 incident, monitoring works, rollback remains available.

---

# Post-Go-Live Backlog

Only after stable real usage consider:
- full-duplex Phone AI
- QuickBooks/Xero deep integration
- advanced route optimization
- predictive inventory
- advanced recommendations
- dynamic pricing
- multi-region HA
- richer BI
- native mobile apps if justified

---

# Final Product-Owner Gates

1. Security — Days 31–33
2. Data & Money Correctness — Days 34–37
3. Database & Multi-Instance Reliability — Days 38–41
4. Observability & Recovery — Days 42–45
5. Repeatable Delivery — Days 46–48
6. Real MVP Providers — Days 49–53
7. UX & Performance — Days 54–57
8. Security & E2E Release Gate — Days 58–60
9. Pilot/UAT — Days 61–63
10. Release & Go-Live — Days 64–66

---

# Daily Completion Report Template

For every day finish with:

1. Baseline state found.
2. Files changed.
3. Database migrations added.
4. Security controls added.
5. Tests added.
6. Test results.
7. Manual verification.
8. Remaining P0/P1 risks.
9. Documentation updated.
10. Exact commands to reproduce.
11. Commit summary.
12. STOP — do not start next day.

---

# Final Principle

The first 30 days built product breadth.

Days 31–66 must prove:

```text
SECURE
+
CORRECT
+
RELIABLE
+
OBSERVABLE
+
RECOVERABLE
+
DEPLOYABLE
+
USABLE
=
PRODUCTION-READY RENTFLOW AI
```

**Do not measure progress only by number of features. Measure progress by number of production risks removed.**
