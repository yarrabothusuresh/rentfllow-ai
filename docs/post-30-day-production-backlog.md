# RentFlow AI — Post-30-Day Production Readiness Backlog & Daily Execution Roadmap

## Executive Summary

A comprehensive post-Day 30 production readiness audit of RentFlow AI was conducted across backend, frontend, database, security, financial transactions, concurrency, AI safety, external integrations, and operations.

While the core SaaS domain models, workflow controllers, and Angular UI features were built during the initial 30 days, **RentFlow AI is currently NOT production-ready**. Multiple critical production blockers (P0) and foundational gaps (P1) were identified that must be resolved before a secure, reliable launch can occur.

---

## 1. Prioritized Production Backlog

### P0 — MUST FIX BEFORE PRODUCTION (Launch Blockers)
*Vulnerabilities and flaws that risk tenant isolation, data breach, money corruption, or system collapse.*

1. **Authentication & Identity Foundation**:
   - `spring-boot-starter-security` is missing from `pom.xml`.
   - The platform lacks password hashing (BCrypt); `User` entity has no password or email.
   - In `CustomerPortalService`, customer passwords are stored and verified in raw plaintext (`cu.setPasswordHash(req.getPassword())`).
   - Portal auth tokens are unverified dummy strings (`demo-portal-token-` + UUID).
2. **Tenant Isolation & Default Role Escalation**:
   - Authentication relies solely on unverified HTTP headers (`X-Tenant-Id`, `X-User-Role`, `X-User-Name`) processed in `TenantContextFilter`.
   - `SecurityUtils` falls back to `DemoDataRepository.EVERGREEN_TENANT_ID` and role `OWNER` for any unauthenticated request. Any unauthenticated caller is treated as an Owner of Evergreen.
   - Any client can forge `X-Tenant-Id: <target-uuid>` to read, update, or delete data belonging to any tenant across all controllers.
3. **Customer Portal IDOR Vulnerability**:
   - Portal endpoints determine customer identity via `@RequestHeader(value = "X-Customer-Id")`.
   - An attacker can view or mutate any customer's bookings, invoices, payments, quotes, and addresses by changing this header. If omitted, it falls back to `CrmDataInitializer.EMILY_CUSTOMER_ID`.
4. **Payment Idempotency & Financial Double-Posting**:
   - `PaymentService.recordPayment` lacks an idempotency check on transaction references.
   - No unique database constraint exists on `(tenant_id, transaction_reference)`. Duplicate webhook deliveries or repeated clicks create multiple payments, causing balance corruption.
5. **Database Migration Safety & Schema Management**:
   - Zero database migration tooling (no Flyway or Liquibase).
   - In dev, Hibernate `ddl-auto=update` is used; in production, `application-prod.properties` specifies `validate`. A fresh PostgreSQL deployment crashes immediately because tables do not exist.
6. **Frontend Container Build Blocker**:
   - `frontend/Dockerfile` does not exist despite being referenced in `docker-compose.yml`. Production container builds fail out of the box.

---

### P1 — REQUIRED FOR PRODUCTION READINESS (Stability & Correctness)
*Capabilities essential for operational integrity, multi-instance safety, and disaster recovery.*

1. **Multi-Instance Scheduler Safety & Distributed Locking**:
   - `@Scheduled` jobs in `IntegrationOutboxProcessor` and `AutomationScheduler` run without distributed locks (no ShedLock or PostgreSQL advisory locks). Multiple replicas cause duplicate processing, duplicate webhooks, and duplicate automation actions.
2. **Rate Limiting Across Public and Sensitive Endpoints**:
   - `RateLimitingService` is implemented as an in-memory token bucket but is ONLY wired to `PhoneCallController`. It is absent on `/auth/login`, public storefront, quote requests, cart operations, webhooks, and AI endpoints.
3. **Database Unique Constraints & Concurrency Hardening**:
   - `Booking.quoteId` and `RentalRequest.idempotencyKey` have database indexes but lack `UNIQUE` constraints scoped to tenant.
   - Reservation temporary holds (`ReservationStatus.HOLD`) have no expiration background scheduler.
4. **Financial Floating-Point Elimination & Authoritative Accounting**:
   - Replace `double` usage in `WorkflowStatusDTO`, `CheckInternalProfitabilityTool`, and AI prompt builders with `BigDecimal`.
   - `CalculateProfitabilityTool` currently falls back to hardcoded mock numbers (e.g. 6480.00 revenue, 2920.00 cost) instead of querying authoritative database tables.
5. **Inbound Webhook Verification & Replay Protection**:
   - `InboundWebhookController` does not verify HMAC-SHA256 signatures. If `X-External-Event-Id` is missing, it auto-generates a UUID, completely defeating deduplication.
6. **Production Observability, Metrics & Structured Logging**:
   - No Spring Boot Actuator, Micrometer, or Prometheus endpoints.
   - No MDC / correlation ID (`X-Request-Id`) filter for distributed request tracing.
   - Unstructured console logs without secret masking or JSON formatting.
7. **Database Backup & Automated Restore Validation**:
   - No automated backup cron or container.
   - Disaster recovery runbook in `OPERATIONS.md` has never been executed or validated against an actual restore test.
8. **CI/CD Automation Pipeline**:
   - Repository has no `.github` workflows or CI pipeline to run tests, linting, migration verification, or container builds automatically on commit/PR.

---

### P2 — IMPORTANT FOR FIRST RELEASE (Provider Integrations & UX Hardening)
*Real-world integrations and production polish required before onboarding live customers.*

1. **Real Payment Provider Integration**:
   - Transition from manual `RecordPaymentDTO` to a real Stripe/Square provider with PaymentIntents, webhook signature validation, and refund processing.
2. **Real Transactional Email Provider**:
   - Replace `MockEmailNotificationProvider` with AWS SES or SendGrid.
3. **Real SMS Notification Provider**:
   - Replace `MockSmsNotificationProvider` with Twilio or equivalent SMS gateway.
4. **Real E-Signature & Contract Lifecycle**:
   - Implement formal `Contract` entity with immutable commercial snapshot, digital signature capture, hash verification, and real PDF generation (replacing stub in `InvoiceDocumentService`).
5. **Production Document/Object Storage**:
   - Implement AWS S3 / MinIO blob storage for contracts, invoices, and inspection damage photos (replacing container-local or missing storage).
6. **Tax Engine Configurator**:
   - Replace hardcoded 8.25% `DemoTaxServiceImpl` with configurable multi-jurisdiction tax table or TaxJar/Avalara integration.
7. **Frontend Test Coverage & Quality**:
   - Expand unit/component tests from 7 specs to comprehensive coverage across checkout, warehouse pick/pack, driver delivery, and payment pages.
8. **Pagination Across All List Endpoints**:
   - Add Spring Data `Pageable` and Angular pagination to products, bookings, quotes, invoices, and customers to eliminate unbounded query memory exhaustion.

---

### P3 — FUTURE / SCALE
*Scale, ecosystem, and advanced optimizations for post-launch growth.*

1. **Twilio Voice / Real Telephony Integration**: Full-duplex WebSocket audio streaming (`wss://`) for Phone AI.
2. **Accounting Provider Integration**: Real QuickBooks Online / Xero two-way sync.
3. **Advanced Route Optimization**: TSP multi-stop routing with Google Maps / Mapbox APIs.
4. **Multi-Region High Availability**: Active-passive failover and cross-region read replicas.

---

## 2. Thirty-Day Daily Execution Roadmap (Days 31–60)

```
[Day 31: Tenant Isolation & IDOR]
               ↓
[Day 32: Payment Idempotency & Financial Integrity]
               ↓
[Day 33: Booking / Checkout / Webhook Idempotency]
               ↓
[Day 34: DB Transactions & Outbox Reliability]
               ↓
[Day 35: Scheduler & Multi-Instance Safety]
               ↓
[Day 36: Auth, Passwords, JWT & Security Headers]
               ↓
[Day 37: AI Security & Prompt Injection Defense]
               ↓
[Day 38: DB Migrations, Indexes & Pagination]
               ↓
[Day 39: Load & Scale Performance Testing]
               ↓
[Day 40: Observability, Metrics & Structured Logs]
               ↓
[Day 41: Backup Automation & Restore Verification]
               ↓
[Day 42: Disaster Recovery & Failure Simulation]
               ↓
[Day 43: CI/CD Pipeline Automation]
               ↓
[Day 44: Staging Environment Deployment]
               ↓
[Day 45: Production Docker & Runtime Hardening]
               ↓
[Days 46-52: Real Provider Integrations (Email, Payments, SMS, Telephony, Accounting, Storage, Tax)]
               ↓
[Days 53-56: Contracts, Frontend Quality & Backend Tuning]
               ↓
[Days 57-60: Security Pentest, E2E Regression, Final Audit & Release Candidate]
```

---

### DAY 31: Multi-Tenant Security + IDOR Hardening
- **Task Number**: 1
- **Title**: Multi-Tenant Security + IDOR Hardening
- **Priority**: P0 (Launch Blocker)
- **Why Needed**: Current architecture trusts raw client headers (`X-Tenant-Id`, `X-Customer-Id`) and defaults to `EVERGREEN_TENANT_ID` with `OWNER` role when absent. Any attacker can read or mutate any tenant's and any customer's data.
- **Current State**: `SecurityUtils` defaults to Evergreen tenant and OWNER role. Controllers resolve tenant/customer from unverified headers. Plaintext comparison in portal auth.
- **Expected Outcome**: All requests require authenticated tokens. Tenant context is cryptographically derived from the verified token. Unauthorized cross-tenant or cross-customer access yields HTTP 401/403.
- **Backend Work**:
  - Remove fallback to `EVERGREEN_TENANT_ID` and `OWNER` from `SecurityUtils`.
  - Update `TenantContextFilter` to extract and verify JWT claims rather than trusting raw request headers.
  - Implement customer-to-tenant verification checks on all portal endpoints.
- **Frontend Work**:
  - Implement Angular HTTP Interceptor to attach Authorization Bearer token.
  - Remove hardcoded `X-Tenant-Id` and `X-User-Role` headers from service files (`booking.service.ts`, `warehouse.service.ts`, etc.).
- **Database Work**: Add database indices and check constraints ensuring all entities have non-null `tenant_id`.
- **Security Work**: Complete OWASP Top 10 IDOR audit for all REST controllers.
- **Tests**: Automated cross-tenant tests (`CrossTenantSecurityTest`) asserting that Tenant A receives 403/404 when querying Tenant B resources. Same-tenant customer IDOR tests.
- **Manual Verification**: Launch Postman/curl, send requests without headers or with foreign tenant IDs, verify strict rejection.
- **Documentation**: Document tenant isolation architecture in `SECURITY.md`.
- **Success Criteria**: Zero cross-tenant data leaks; unauthenticated requests receive 401 Unauthorized; portal users cannot access another customer's ID.
- **Do Not Build**: Full OAuth2/SSO provider integrations.

---

### DAY 32: Payment Idempotency + Financial Transaction Integrity
- **Task Number**: 2
- **Title**: Payment Idempotency + Financial Transaction Integrity
- **Priority**: P0 (Launch Blocker)
- **Why Needed**: Repeated payment callbacks or rapid user clicks currently create duplicate payments, overpay bookings, and corrupt accounting balances.
- **Current State**: `PaymentService.recordPayment` does not verify whether `transactionReference` already exists. No unique constraint on `(tenant_id, transaction_reference)`.
- **Expected Outcome**: Payment recording is strictly idempotent. Repeated submission with the same transaction reference returns existing payment with no duplicate balance deduction.
- **Backend Work**:
  - Add deduplication check in `PaymentService.recordPayment` using `findByTenantIdAndTransactionReference`.
  - Ensure booking balance updates and invoice payment synchronizations execute inside a single atomic transaction.
  - Eliminate all remaining `double`/`float` in financial calculation paths.
- **Frontend Work**:
  - Disable payment submit buttons upon click; add visual processing indicators.
  - Display existing payment receipt when receiving idempotent responses.
- **Database Work**: Add unique constraint `uk_payment_tenant_txref` on `payment(tenant_id, transaction_reference)` where transaction reference is non-null.
- **Security Work**: Prevent negative amount payments and overpayment manipulation.
- **Tests**: Concurrency unit tests firing 10 concurrent requests with the same transaction reference; assert exactly 1 record created.
- **Manual Verification**: Execute duplicate POST `/api/bookings/{id}/payments` via curl, verify balance is deducted only once.
- **Documentation**: Update payment processing flow in `docs/payments.md`.
- **Success Criteria**: 100% idempotent payment recording under concurrent execution; zero balance discrepancies.
- **Do Not Build**: Live Stripe API keys integration.

---

### DAY 33: Booking, Checkout, and Webhook Idempotency
- **Task Number**: 3
- **Title**: Booking, Checkout, and Webhook Idempotency
- **Priority**: P0 / P1
- **Why Needed**: Double-clicking "Book Now", retrying public quote submissions, or repeated webhook deliveries must never create duplicate bookings, leads, or outbox dispatches.
- **Current State**: `Booking.quoteId` has no unique constraint. `PublicQuoteRequestService` creates new guest customers and quotes on every click without deduplication. `InboundWebhookController` generates random IDs if header is missing.
- **Expected Outcome**: Quotes convert to bookings at most once. Public checkout submissions with an idempotency key return identical responses. Inbound webhooks require external IDs and deduplicate cleanly.
- **Backend Work**:
  - Add idempotency key support to `PublicQuoteRequestDTO` and store in `RentalRequest`.
  - Reject inbound webhooks that lack an external event identifier.
  - Ensure canonical lock in `BookingService.createBookingFromQuote` guards against concurrent double-booking.
- **Frontend Work**:
  - Generate client UUID idempotency keys on checkout form load; resend same key on network retry.
- **Database Work**:
  - Add unique constraint on `booking(tenant_id, quote_id)`.
  - Add unique constraint on `rental_requests(tenant_id, idempotency_key)`.
- **Security Work**: Defend against replay attacks on public quote submissions.
- **Tests**: Concurrency test attempting simultaneous `createBookingFromQuote` calls; verify only 1 booking succeeds.
- **Manual Verification**: Simulate rapid double-click in storefront checkout; confirm single booking/request in DB.
- **Documentation**: Add idempotency contract specification to `docs/rental-workflow.md`.
- **Success Criteria**: Zero duplicate bookings or duplicate rental requests under network retries.
- **Do Not Build**: Third-party checkout iframe components.

---

### DAY 34: Database Transaction & Outbox Reliability
- **Task Number**: 4
- **Title**: Database Transaction & Outbox Reliability
- **Priority**: P1
- **Why Needed**: Core state updates must never roll back due to external webhook delivery failures, and outbox messages must never be lost if the database transaction commits.
- **Current State**: Outbox repository saves events, but processor dispatches synchronously within `@Scheduled` loops without isolation from external connector exceptions.
- **Expected Outcome**: Clean separation of transaction commit and external outbox dispatch. Failed webhook deliveries are retried via exponential backoff and dead-lettered after max attempts.
- **Backend Work**:
  - Ensure all domain events (`PaymentReceivedEvent`, `BookingConfirmedEvent`) write to `IntegrationOutbox` in the same `@Transactional` boundary as domain state changes.
  - Isolate each outbox dispatch in `IntegrationOutboxProcessor` using `REQUIRES_NEW` transaction propagation.
- **Frontend Work**: Display outbox / webhook sync health indicators in integration settings UI.
- **Database Work**: Verify indexes on `integration_outbox(status, next_attempt_at)`.
- **Security Work**: Sanitize outbox payload to prevent leaking sensitive credentials in dead-letter logs.
- **Tests**: Test throwing simulated network exception during webhook delivery; verify core booking/payment commits while outbox event status shifts to `PENDING` with retry backoff.
- **Manual Verification**: Trigger order creation, observe database commit, verify outbox record processing in background.
- **Documentation**: Update `docs/events.md` with transactional outbox guarantees.
- **Success Criteria**: Zero partial writes; outbox guaranteed delivery or DLQ logging without blocking core transactions.
- **Do Not Build**: Kafka or RabbitMQ infrastructure.

---

### DAY 35: Scheduler & Multi-Instance Safety
- **Task Number**: 5
- **Title**: Scheduler & Multi-Instance Safety
- **Priority**: P1
- **Why Needed**: When running multiple backend replicas in production, uncoordinated `@Scheduled` jobs will execute concurrently on every node, firing duplicate webhooks and duplicate automation actions.
- **Current State**: `IntegrationOutboxProcessor` (every 5s) and `AutomationScheduler` (every 5m) run naked `@Scheduled` tasks without distributed locks.
- **Expected Outcome**: Scheduled jobs are executed by exactly one replica per cluster interval using distributed database locking.
- **Backend Work**:
  - Introduce ShedLock (`shedlock-spring` + `shedlock-provider-jdbc-template`) or PostgreSQL advisory locking.
  - Annotate `@Scheduled` methods with `@SchedulerLock`.
  - Add hold expiration background job to transition expired inventory holds to `EXPIRED`.
  - Add automated overdue invoice scheduler.
- **Frontend Work**: None.
- **Database Work**: Create `shedlock` table (`name`, `lock_until`, `locked_at`, `locked_by`).
- **Security Work**: Prevent lock starvation and ensure dead replicas release locks automatically.
- **Tests**: Multi-thread / multi-instance simulation test verifying that two simultaneous scheduler executions run only once.
- **Manual Verification**: Run two instances of backend on ports 8080 and 8081 against same Postgres DB; verify single execution in logs.
- **Documentation**: Update `OPERATIONS.md` with multi-replica clustering requirements.
- **Success Criteria**: Zero duplicate scheduler runs across multi-container deployments.
- **Do Not Build**: ZooKeeper or complex distributed lock managers.

---

### DAY 36: Secrets, Passwords, JWT & Security Hardening
- **Task Number**: 6
- **Title**: Secrets, Passwords, JWT & Security Hardening
- **Priority**: P0 (Launch Blocker)
- **Why Needed**: Customer passwords are saved in plaintext; staff users have no passwords; JWT validation is completely missing; CORS is wildcarded in several controllers.
- **Current State**: Plaintext `cu.setPasswordHash(password)`; `spring-boot-starter-security` absent; `@CrossOrigin(originPatterns = "*")` on `PaymentController`, `ProductController`, `HealthController`.
- **Expected Outcome**: Standard BCrypt password hashing; Spring Security filter chain with HMAC/RSA JWT token parsing; strict CORS origin whitelist.
- **Backend Work**:
  - Add `spring-boot-starter-security` and `jjwt` to `pom.xml`.
  - Add `passwordHash` and `email` to `User` entity.
  - Implement `PasswordEncoder` (BCrypt 12 rounds) across staff and customer portal authentication.
  - Implement `JwtAuthenticationFilter` verifying Bearer tokens and setting SecurityContext.
  - Restrict CORS globally to `security.cors.allowed-origins` and remove wildcard `@CrossOrigin` annotations.
- **Frontend Work**:
  - Implement secure login forms with password validation.
  - Store JWT in secure HttpOnly cookie or browser session storage; clear on logout.
- **Database Work**: Add `password_hash` column to `app_user` table.
- **Security Work**: Enforce minimum password strength (8+ chars, numbers, symbols); enforce HTTPS redirect in production.
- **Tests**: Security test verifying BCrypt hashes; test verifying expired/tampered JWT tokens return 401; test verifying rejected cross-origin requests.
- **Manual Verification**: Attempt login with wrong password; verify rejection; inspect database to ensure no plaintext passwords exist.
- **Documentation**: Update `SECURITY.md` with authentication architecture and token life cycles.
- **Success Criteria**: Zero plaintext passwords in database; all protected API endpoints require valid JWT; strict CORS headers.
- **Do Not Build**: Biometric or hardware FIDO2 authentication.

---

### DAY 37: AI Security & Prompt-Injection Defense
- **Task Number**: 7
- **Title**: AI Security & Prompt-Injection Defense
- **Priority**: P1
- **Why Needed**: Public users interacting with the AI Sales Agent must not extract internal pricing margins, customer PII, tenant data, or trigger unauthorized mutations via prompt injections.
- **Current State**: Role checks in `AiSalesToolRegistry` restrict internal tools, but prompt sanitization and prompt injection testing are unverified.
- **Expected Outcome**: Comprehensive boundary defense where LLM outputs are treated as untrusted; tool parameters are strictly validated; prompt injection attempts cannot bypass server authorization.
- **Backend Work**:
  - Implement input sanitization removing system directive markers (`[SYSTEM]`, `<prompt_injection>`, `IGNORE PREVIOUS INSTRUCTIONS`).
  - Add outbound response filter stripping internal keywords (margin, cost, profit, internal notes) if customer role is active.
  - Implement per-tenant daily AI token usage tracking and circuit breaker.
- **Frontend Work**: Add rate limit and warning messages in AI Sales chat UI when spam or anomalous inputs are detected.
- **Database Work**: Add table `ai_token_usage(tenant_id, date, request_count, token_count)`.
- **Security Work**: Conduct adversarial prompt injection suite (jailbreak attempts, role assumption, SQL leakage, PII scraping).
- **Tests**: `AiPromptInjectionSecurityTest` executing 15 attack vectors; assert 0% leak of internal metrics or cross-tenant bookings.
- **Manual Verification**: Chat with AI agent in portal using adversarial prompts; confirm agent refuses and logs security alert.
- **Documentation**: Document AI Safety Guardrails in `docs/ai-architecture.md`.
- **Success Criteria**: 100% rejection of prompt injection exploits; zero financial margin leaks to customer role.
- **Do Not Build**: Self-hosted custom LLM training pipelines.

---

### DAY 38: Database Migrations, Indexes & Pagination
- **Task Number**: 8
- **Title**: Database Migrations, Indexes & Pagination
- **Priority**: P0 / P1
- **Why Needed**: Hibernate auto-DDL cannot be safely used in production. Lack of pagination on list endpoints creates memory leaks under moderate data scale.
- **Current State**: No Flyway/Liquibase. `ddl-auto=validate` fails on blank Postgres. All `getProducts()`, `getBookings()`, `getInvoices()` return unbounded lists.
- **Expected Outcome**: Versioned Flyway migrations manage schema creation and updates. All high-volume list endpoints support `Pageable` with default page size 25.
- **Backend Work**:
  - Add `flyway-core` and `flyway-database-postgresql` to `pom.xml`.
  - Create baseline migration `V1__initial_schema.sql` encompassing all entities, foreign keys, unique constraints, and indexes.
  - Refactor `ProductController`, `BookingController`, `CustomerController`, `QuoteController`, `InvoiceController` to accept `Pageable`.
- **Frontend Work**: Update Angular list tables to support pagination controls (page number, page size, total elements).
- **Database Work**: Create `db/migration` scripts; add missing composite indexes on `(tenant_id, created_at)` and `(tenant_id, status)`.
- **Security Work**: Restrict max page size to 100 to prevent DoS via `?size=1000000`.
- **Tests**: Flyway migration test against test container; pagination tests verifying sliced data.
- **Manual Verification**: Run migration on empty Postgres database; confirm application boots with `ddl-auto=validate`.
- **Documentation**: Create migration maintenance guide in `docs/database-migrations.md`.
- **Success Criteria**: Fresh Postgres boots cleanly with Flyway V1; all major list endpoints return `Page<DTO>` with bounded limits.
- **Do Not Build**: Dynamic schema generator tools.

---

### DAY 39: Large Dataset & Load Performance Testing
- **Task Number**: 9
- **Title**: Large Dataset & Load Performance Testing
- **Priority**: P1
- **Why Needed**: Detect N+1 query problems, memory leaks, and connection pool exhaustion before production traffic arrives.
- **Current State**: Only small unit test fixtures exist; performance under 1,000+ bookings or 5,000 products is unmeasured.
- **Expected Outcome**: Verified sub-200ms latency on public catalog, availability checks, and booking creation under concurrent load.
- **Backend Work**:
  - Optimize repository queries with `JOIN FETCH` or `@EntityGraph` to eliminate N+1 fetches on bookings, quotes, and warehouse orders.
  - Tune HikariCP connection pool settings (`maximumPoolSize=20`, `minimumIdle=5`).
- **Frontend Work**: None.
- **Database Work**: Run `EXPLAIN ANALYZE` on availability calculation queries; optimize range scans.
- **Security Work**: Verify database pool starvation does not crash the JVM.
- **Tests**: JMeter or Gatling test script simulating:
  - 50 concurrent storefront browsers
  - 20 concurrent availability checks
  - 5 concurrent checkouts
- **Manual Verification**: Seed 5,000 products and 10,000 bookings in test DB; load catalog and dashboard; verify response < 300ms.
- **Documentation**: Publish benchmark report in `docs/performance-benchmarks.md`.
- **Success Criteria**: 95th percentile HTTP latency < 300ms; zero out-of-memory errors; Hikari pool operates without starvation.
- **Do Not Build**: Multi-cluster distributed caching grid.

---

### DAY 40: Observability, Metrics & Operational Alerts
- **Task Number**: 10
- **Title**: Observability, Metrics & Operational Alerts
- **Priority**: P1
- **Why Needed**: Production has no way to monitor error rate spikes, database pool health, webhook failures, or AI latency in real time.
- **Current State**: Custom `HealthController` without Actuator; no Prometheus metrics; no correlation IDs; no log masking.
- **Expected Outcome**: Standard Spring Boot Actuator with `/actuator/prometheus` metrics, correlation ID tracing on all requests, and structured JSON logs.
- **Backend Work**:
  - Add `spring-boot-starter-actuator` and `micrometer-registry-prometheus` to `pom.xml`.
  - Implement `MdcCorrelationIdFilter` generating and propagating `X-Request-Id`.
  - Add `logback-spring.xml` with JSON encoder and secret masking (redact `password`, `token`, `secret`, `authorization`).
- **Frontend Work**: Propagate incoming `X-Request-Id` in error notifications to allow users to quote trace IDs to support.
- **Database Work**: None.
- **Security Work**: Verify that API keys, passwords, and tokens are completely masked in console/file logs.
- **Tests**: Test verifying `X-Request-Id` is present in response headers and MDC; test verifying `/actuator/prometheus` produces valid metrics.
- **Manual Verification**: Call an endpoint; inspect log output for JSON structure and matching request ID; check `/actuator/prometheus`.
- **Documentation**: Document monitoring setup and metric names in `OPERATIONS.md`.
- **Success Criteria**: Prometheus metrics scrapeable; correlation IDs attached to 100% of HTTP requests; secrets masked.
- **Do Not Build**: Full Grafana cloud dashboard deployment.

---

### DAY 41: Backup Automation & Restore Verification
- **Task Number**: 11
- **Title**: Backup Automation & Restore Verification
- **Priority**: P1
- **Why Needed**: A backup strategy that has never been automated or restored is invalid.
- **Current State**: Markdown snippets in `OPERATIONS.md`; no automated backup container or executable restore verification script.
- **Expected Outcome**: Automated backup script producing compressed, timestamped, encrypted PostgreSQL dumps, coupled with an executable restore test script.
- **Backend Work**: None.
- **Frontend Work**: None.
- **Database Work**:
  - Create executable backup script `scripts/backup.sh` utilizing `pg_dump -Fc`.
  - Create executable restore test script `scripts/restore-test.sh` that loads a dump into an ephemeral test DB and validates row counts.
- **Security Work**: Encrypt backups using AES-256 (GPG); restrict backup file permissions to `0600`.
- **Tests**: CI or local test running `backup.sh` followed by `restore-test.sh` asserting exact record matching across tenants.
- **Manual Verification**: Execute backup, drop test DB, run restore script, verify application reads restored bookings and products.
- **Documentation**: Update `OPERATIONS.md` with verified step-by-step restore instructions.
- **Success Criteria**: Backup script runs cleanly; automated restore script succeeds and verifies table checksums.
- **Do Not Build**: Proprietary commercial backup appliances.

---

### DAY 42: Disaster Recovery & Failure Simulation
- **Task Number**: 12
- **Title**: Disaster Recovery & Failure Simulation
- **Priority**: P1
- **Why Needed**: System must behave predictably and fail gracefully when third-party dependencies (AI API, email, DB connection) experience outages.
- **Current State**: Unhandled exceptions in downstream services can bubble up as generic HTTP 500s.
- **Expected Outcome**: Graceful degradation: AI outages fallback to deterministic search; email failures do not abort core checkout; database disconnects trigger HTTP 503 readiness failure.
- **Backend Work**:
  - Implement resilience circuit breakers on external AI client calls.
  - Implement global exception handler returning standard `ProblemDetail` (RFC 7807) without stack traces.
- **Frontend Work**: Display user-friendly error banners ("AI assistant temporarily unavailable, browsing catalog directly") instead of white screens.
- **Database Work**: None.
- **Security Work**: Prevent stack trace or database query leakage in error responses.
- **Tests**: Failure simulation integration test injecting faults into AI provider and email sender; verify core booking flow completes.
- **Manual Verification**: Turn off network/mock AI key; execute checkout in UI; verify user can complete booking with deterministic fallback.
- **Documentation**: Publish Disaster Recovery playbook in `OPERATIONS.md`.
- **Success Criteria**: Core rental lifecycle functional during external provider outages; zero leaked internal stack traces.
- **Do Not Build**: Multi-region live-active database clusters.

---

### DAY 43: CI/CD Pipeline Automation
- **Task Number**: 13
- **Title**: CI/CD Pipeline Automation
- **Priority**: P1
- **Why Needed**: Automated quality gates are required on every pull request to prevent regressions in security, builds, and tests.
- **Current State**: No `.github` directory exists; all builds and tests must be executed manually.
- **Expected Outcome**: GitHub Actions workflow running backend unit & integration tests, frontend build & Karma tests, and container image builds.
- **Backend Work**: Ensure Maven runs in batch mode (`-B`) cleanly without interactive prompts.
- **Frontend Work**: Ensure `ng test --watch=false --browsers=ChromeHeadless` runs in Linux container.
- **Database Work**: Run Postgres service container in CI workflow for integration test execution.
- **Security Work**: Add `dependency-check` or CodeQL scanning step in CI.
- **Tests**: Validate workflow run on GitHub Actions environment.
- **Manual Verification**: Trigger PR or commit, observe automated green checkmarks on GitHub.
- **Documentation**: Add pipeline architecture to `README.md`.
- **Success Criteria**: Automated CI workflow executing on pull request in < 5 minutes; blocking PR merge on test failure.
- **Do Not Build**: Multi-cloud deployment orchestrators.

---

### DAY 44: Staging Environment & Deployment Rehearsal
- **Task Number**: 14
- **Title**: Staging Environment & Deployment Rehearsal
- **Priority**: P1
- **Why Needed**: An identical production-like environment is needed to rehearse migrations, configuration variables, and smoke tests before launching to real users.
- **Current State**: System only tested locally on developer workstations using H2 or local Postgres.
- **Expected Outcome**: Containerized staging environment running with PostgreSQL, production backend profile, and NGINX frontend.
- **Backend Work**: Validate `application-prod.properties` with environment variables in staging container.
- **Frontend Work**: Build production bundle with staging API base URL.
- **Database Work**: Run initial Flyway migrations against staging PostgreSQL instance.
- **Security Work**: Ensure staging uses separate non-production secrets and credentials.
- **Tests**: Run automated smoke test suite against staging URL (`GET /api/health`, `GET /api/public/products`, etc.).
- **Manual Verification**: Access staging URL via browser, perform login, create product, book rental.
- **Documentation**: Document staging deployment runbook in `DEPLOYMENT.md`.
- **Success Criteria**: Staging deployed and responding with HTTP 200; smoke test suite 100% green.
- **Do Not Build**: Kubernetes Helm charts if Docker Compose staging suffices for launch.

---

### DAY 45: Production Docker & Runtime Hardening
- **Task Number**: 15
- **Title**: Production Docker & Runtime Hardening
- **Priority**: P0 / P1
- **Why Needed**: `frontend/Dockerfile` is missing, causing `docker-compose.yml` build failure. Container images must be hardened against root privilege escalation.
- **Current State**: Backend Dockerfile exists (uses non-root user). Frontend Dockerfile is completely absent.
- **Expected Outcome**: Multi-stage `frontend/Dockerfile` utilizing NGINX alpine; hardened non-root execution; healthy compose startup.
- **Backend Work**: Verify memory limits and JVM opts (`-XX:+UseContainerSupport`, `-XX:MaxRAMPercentage=75.0`).
- **Frontend Work**: Create `frontend/Dockerfile` (Stage 1: node build; Stage 2: nginx:alpine with custom `nginx.conf` supporting Angular routing).
- **Database Work**: Configure persistent named volume for Postgres data with proper ownership permissions.
- **Security Work**: Run container vulnerability scan; drop unnecessary Linux capabilities (`CAP_DROP ALL`).
- **Tests**: Run `docker compose build` and `docker compose up -d`; verify all three containers report healthy status.
- **Manual Verification**: Navigate to `http://localhost:80` in browser; verify frontend loads and communicates with backend on port 8080.
- **Documentation**: Update `docker-compose.yml` and container setup instructions in `README.md`.
- **Success Criteria**: `docker compose build` succeeds with zero errors; full stack boots and passes health checks.
- **Do Not Build**: Custom Linux kernel modules.

---

### DAY 46: Real Transactional Email Provider
- **Task Number**: 16
- **Title**: Real Transactional Email Provider
- **Priority**: P2
- **Why Needed**: Customers and staff currently only receive notifications via in-memory mock providers (`MockEmailNotificationProvider`).
- **Current State**: `EmailNotificationProvider` interface backed by `MockEmailNotificationProvider` (logs to console only).
- **Expected Outcome**: Production email provider (AWS SES or SendGrid) delivering quotes, booking confirmations, and invoice reminders with fallback logging.
- **Backend Work**:
  - Implement `SendGridEmailProvider` or `AwsSesEmailProvider` implementing `EmailNotificationProvider`.
  - Add configuration properties (`notification.email.provider`, `notification.email.api-key`, `notification.email.from-address`).
  - Implement retry and failure handling without blocking calling threads.
- **Frontend Work**: Add email delivery status badge in notification log UI.
- **Database Work**: Add `external_message_id` to notification audit tables.
- **Security Work**: Protect API keys via environment variables; validate sender SPF/DKIM alignment.
- **Tests**: Integration test using WireMock or provider sandbox verifying email payload structure and error recovery.
- **Manual Verification**: Trigger quote send in staging; verify email received in real test inbox.
- **Documentation**: Add email provider setup guide in `docs/notifications.md`.
- **Success Criteria**: Verified email delivery to real recipient inbox; non-blocking asynchronous dispatch.
- **Do Not Build**: Marketing newsletter campaign builder.

---

### DAY 47: Real Payment Provider Integration (Stripe)
- **Task Number**: 17
- **Title**: Real Payment Provider Integration (Stripe)
- **Priority**: P2
- **Why Needed**: Production launch requires automated credit card collection and deposit authorization rather than manual payment recording.
- **Current State**: Manual `PaymentService.recordPayment` with mock methods; `InboundWebhookService` has stub comment for Stripe.
- **Expected Outcome**: Integrated Stripe PaymentIntents API for online checkout deposits, with verified webhook signature handling.
- **Backend Work**:
  - Add `stripe-java` SDK to `pom.xml`.
  - Implement `StripePaymentService` creating PaymentIntents with tenant-scoped metadata.
  - Implement `StripeWebhookHandler` validating `Stripe-Signature` HMAC header and triggering `recordPayment` idempotently on `payment_intent.succeeded`.
- **Frontend Work**:
  - Embed Stripe Elements in customer portal and storefront checkout for secure card entry.
- **Database Work**: Add `stripe_payment_intent_id` and `stripe_customer_id` columns to `payment` and `customers`.
- **Security Work**: Strict PCI-DSS compliance: card numbers never touch RentFlow servers; verify webhook cryptographic signatures.
- **Tests**: Stripe mock webhook test verifying signature validation, successful payment recording, and duplicate replay prevention.
- **Manual Verification**: Run test card transaction in Stripe test mode; observe automatic booking balance update in portal.
- **Documentation**: Publish payment integration guide in `docs/payments.md`.
- **Success Criteria**: Real card charged in Stripe test environment; webhook updates booking balance in real time.
- **Do Not Build**: Custom stored value cards or physical POS chip-and-pin hardware.

---

### DAY 48: Real SMS Notification Provider
- **Task Number**: 18
- **Title**: Real SMS Notification Provider
- **Priority**: P2
- **Why Needed**: Critical delivery arrival alerts and driver dispatch updates need real SMS delivery to customer mobile numbers.
- **Current State**: `MockSmsNotificationProvider` logs to console.
- **Expected Outcome**: Twilio SMS provider sending delivery arrival notices and quote reminders.
- **Backend Work**:
  - Implement `TwilioSmsProvider` implementing `SmsNotificationProvider`.
  - Configure `notification.sms.account-sid`, `auth-token`, and `from-number`.
  - Validate E.164 phone number formatting before dispatch.
- **Frontend Work**: Add SMS notification toggle in customer portal communication preferences.
- **Database Work**: None.
- **Security Work**: Rate limit outbound SMS per recipient number to avoid spam abuse.
- **Tests**: Unit test verifying phone number formatting and error handling on Twilio 400 response.
- **Manual Verification**: Trigger delivery status `EN_ROUTE`; verify SMS received on test mobile device.
- **Documentation**: Update `docs/notifications.md` with Twilio credentials setup.
- **Success Criteria**: SMS successfully delivered to verified test phone number; failures logged cleanly.
- **Do Not Build**: Inbound two-way conversational SMS chatbot.

---

### DAY 49: Real Telephony / Phone AI Provider Foundation
- **Task Number**: 19
- **Title**: Real Telephony / Phone AI Provider Foundation
- **Priority**: P2
- **Why Needed**: Phone AI currently runs on `MockTelephonyProvider` with simulated call turns. Production requires connecting to real inbound phone numbers.
- **Current State**: `TelephonyProvider` interface with `MockTelephonyProvider` and HTTP turn-based controllers.
- **Expected Outcome**: Inbound Twilio Voice webhook integration handling call initiation, TwiML `<Gather>` speech-to-text, and conversational handoff.
- **Backend Work**:
  - Implement `TwilioVoiceProvider` generating valid TwiML responses.
  - Implement webhook signature verification on inbound voice webhook calls.
  - Connect transcribed caller text to `PhoneAiConversationService`.
- **Frontend Work**: Live call audio indicator in staff Phone AI dashboard.
- **Database Work**: Store external `call_sid` in `phone_calls` table.
- **Security Work**: Ensure call recordings are disabled by default unless customer consents; secure webhook endpoints.
- **Tests**: Mock TwiML generation test verifying correct prompt and speech gathering tags.
- **Manual Verification**: Dial Twilio test phone number; speak rental equipment inquiry; verify response generated and logged.
- **Documentation**: Add telephony setup and TwiML webhook URL configuration to `docs/day-30-production-readiness.md`.
- **Success Criteria**: Inbound call answers, transcribes speech, generates rental availability response, and hangs up or transfers.
- **Do Not Build**: Custom on-premise Asterisk PBX switches.

---

### DAY 50: Accounting Connector Production Integration
- **Task Number**: 20
- **Title**: Accounting Connector Production Integration
- **Priority**: P2
- **Why Needed**: Rental invoices and payments must sync to external accounting systems (QuickBooks Online / Xero) to avoid double-entry bookkeeping.
- **Current State**: `MockAccountingConnector` only. No real OAuth2 or API integration.
- **Expected Outcome**: QuickBooks Online OAuth2 token management and asynchronous invoice/payment push via integration outbox.
- **Backend Work**:
  - Implement QuickBooks Online REST client syncing `Invoice` and `Payment` entities.
  - Implement OAuth2 token refresh flow for accounting connections.
- **Frontend Work**: "Connect to QuickBooks" OAuth popup button in Integration Settings page.
- **Database Work**: Add encrypted `access_token`, `refresh_token`, and `realm_id` columns in `integration_connections`.
- **Security Work**: Encrypt OAuth tokens at rest using AES-GCM-256.
- **Tests**: Mock HTTP test simulating QuickBooks API responses and token refresh cycles.
- **Manual Verification**: Click connect, authorize sandbox QuickBooks account, create invoice in RentFlow, verify invoice appears in QB sandbox.
- **Documentation**: Create accounting connector guide in `docs/day-25.md`.
- **Success Criteria**: Completed invoice and payment synced to QuickBooks Online sandbox with matching dollar amounts.
- **Do Not Build**: Multi-currency complex FX hedging logic.

---

### DAY 51: Document & Object Storage Integration (S3/MinIO)
- **Task Number**: 21
- **Title**: Document & Object Storage Integration (S3/MinIO)
- **Priority**: P2
- **Why Needed**: Contracts, inspection photos, and invoice PDFs cannot be stored on container local disk because containers are ephemeral.
- **Current State**: Zero document storage infrastructure. File attachments not stored.
- **Expected Outcome**: Pluggable `DocumentStorageService` (AWS S3 or S3-compatible MinIO) generating pre-signed URLs for uploads and downloads.
- **Backend Work**:
  - Implement `S3DocumentStorageService` using AWS S3 SDK v2.
  - Add upload endpoints for damage inspection photos and contract signatures.
  - Generate expiring pre-signed GET URLs for customer file access.
- **Frontend Work**: Photo upload component on mobile return inspection page.
- **Database Work**: Create `document_attachments(id, tenant_id, entity_type, entity_id, file_name, s3_key, mime_type, file_size)`.
- **Security Work**: Enforce strict MIME type validation; scan file extensions to prevent arbitrary file upload vulnerabilities.
- **Tests**: Local MinIO container test verifying file upload, checksum verification, and pre-signed URL retrieval.
- **Manual Verification**: Upload photo during return inspection; verify photo stored in S3/MinIO bucket and visible in dashboard.
- **Documentation**: Document storage bucket policies and environment variables in `OPERATIONS.md`.
- **Success Criteria**: Inspection photos and contract documents stored in durable cloud storage; zero container filesystem dependence.
- **Do Not Build**: Cloud video streaming transcoders.

---

### DAY 52: Tax Engine Production Readiness
- **Task Number**: 22
- **Title**: Tax Engine Production Readiness
- **Priority**: P2
- **Why Needed**: Hardcoded 8.25% demo tax is illegal in multi-jurisdiction production rental businesses where tax rates vary by state, county, and city.
- **Current State**: `DemoTaxServiceImpl` hardcodes flat 8.25%.
- **Expected Outcome**: Configurable tax rules per tenant with zip-code/state lookup table and support for tax-exempt customers.
- **Backend Work**:
  - Replace `DemoTaxServiceImpl` with `ConfigurableTaxService`.
  - Add `tax_exempt` boolean on `Customer` entity.
  - Calculate tax based on delivery address postal code and rental category taxable flags.
- **Frontend Work**: Add Tax Settings configuration page allowing tenant admins to set tax rates and manage exemptions.
- **Database Work**: Create `tenant_tax_rules(id, tenant_id, state, zip_code, tax_rate, active)`.
- **Security Work**: Ensure customer cannot manipulate tax percentage in request payload; calculations must remain strictly backend-authoritative.
- **Tests**: Unit tests verifying tax exemption logic, zero-rate jurisdictions, and boundary rounding with `RoundingMode.HALF_UP`.
- **Manual Verification**: Set up 7% tax for Tenant A; create quote in that jurisdiction; verify exact tax calculation on invoice.
- **Documentation**: Document tax engine configuration in `docs/day-12.md`.
- **Success Criteria**: Correct multi-jurisdiction tax calculation applied to quotes and invoices; tax exemptions honored.
- **Do Not Build**: International VAT border-crossing customs integration.

---

### DAY 53: E-Signature & Contract Production Review
- **Task Number**: 23
- **Title**: E-Signature & Contract Production Review
- **Priority**: P2
- **Why Needed**: Contracts currently consist of a single boolean `contractSigned = false` on `Booking` with no legal document generation, hash verification, or signature capture.
- **Current State**: `InvoiceDocumentService` returns stub "NOT_IMPLEMENTED"; no contract entity exists.
- **Expected Outcome**: Real PDF generation using OpenPDF/iText; capture of customer signature timestamp, IP address, user agent, and SHA-256 document hash.
- **Backend Work**:
  - Implement `RentalContract` entity with immutable snapshot of items, dates, and pricing.
  - Implement `PdfDocumentService` generating standard PDF contracts.
  - Implement signature endpoint recording signing audit trail and cryptographic hash.
- **Frontend Work**: Canvas signature pad component in Customer Portal allowing signature on glass.
- **Database Work**: Create `contracts(id, tenant_id, booking_id, contract_number, document_hash, signed_at, signer_name, signer_ip, signature_image_url)`.
- **Security Work**: Prevent tampering by generating SHA-256 hash of contract text prior to signing; verify signature cannot be applied twice.
- **Tests**: Automated PDF generation test verifying byte stream validity; test verifying signature hash integrity.
- **Manual Verification**: Sign contract in customer portal; download generated PDF; verify signature image and terms embedded.
- **Documentation**: Update `docs/rental-workflow.md` with contract legal audit trail specifications.
- **Success Criteria**: Legally defensible contract document generated, hashed, and signed with immutable audit trail.
- **Do Not Build**: Full DocuSign API integration if internal canvas signature with hash audit suffices for initial launch.

---

### DAY 54: Accessibility & Mobile UX Hardening
- **Task Number**: 24
- **Title**: Accessibility & Mobile UX Hardening
- **Priority**: P2
- **Why Needed**: Warehouse workers and delivery drivers use mobile devices under bright sunlight and challenging conditions; accessibility standards (WCAG 2.1 AA) must be respected.
- **Current State**: Desktop-focused layouts; limited ARIA semantics; minimal mobile viewport optimization on warehouse pick/pack screens.
- **Expected Outcome**: Fully responsive mobile views for warehouse picking, delivery driver checklists, and customer portal; WCAG 2.1 AA contrast and keyboard navigation.
- **Backend Work**: None.
- **Frontend Work**:
  - Optimize warehouse pick-list and delivery checklist views for 375px mobile screens with large tap targets (48x48px min).
  - Add ARIA attributes (`aria-label`, `aria-expanded`, `role="alert"`) to interactive elements and modal dialogs.
  - Ensure high-contrast color ratios across status badges and buttons.
- **Database Work**: None.
- **Security Work**: Ensure mobile interfaces do not store unencrypted sensitive data in localStorage.
- **Tests**: Axe-core automated accessibility audit; responsive visual regression tests.
- **Manual Verification**: Open warehouse pick-list on mobile viewport; complete picking flow with one thumb; navigate via keyboard only.
- **Documentation**: Add Accessibility Guidelines to `docs/UX.html`.
- **Success Criteria**: Zero high-severity accessibility violations on Axe scan; warehouse and driver flows 100% usable on mobile.
- **Do Not Build**: Native iOS/Android Swift/Kotlin apps.

---

### DAY 55: Frontend Performance & Error UX Polish
- **Task Number**: 25
- **Title**: Frontend Performance & Error UX Polish
- **Priority**: P2
- **Why Needed**: Frontend currently lacks global HTTP error interception, displaying silent console errors or raw API strings when errors occur.
- **Current State**: 7 unit test files; template warnings regarding optional chaining in `analytics-dashboard.component.ts`; ad-hoc error handling in components.
- **Expected Outcome**: Unified global HTTP error handler with user-friendly toast notifications; clean bundle with zero template warnings; bundle size optimization.
- **Backend Work**: Standardize all backend error responses into RFC 7807 `ProblemDetail`.
- **Frontend Work**:
  - Implement `GlobalErrorHandler` and `HttpErrorInterceptor` showing toast messages for 400, 401, 403, 404, and 500 errors.
  - Resolve template optional chaining warnings in analytics dashboard.
  - Add skeleton loading states on catalog, bookings, and dashboard cards.
  - Implement route code-splitting / lazy loading across all module routes.
- **Database Work**: None.
- **Security Work**: Ensure internal server error messages never display raw SQL or class names to users.
- **Tests**: Unit tests for `HttpErrorInterceptor` simulating various HTTP error statuses.
- **Manual Verification**: Disconnect network or trigger 500 error; verify clean error toast appears and UI remains stable.
- **Documentation**: Document frontend architecture and error conventions in `frontend/README.md`.
- **Success Criteria**: Zero Angular build warnings; user-friendly error toasts on all failures; initial JS bundle < 2MB.
- **Do Not Build**: Custom web-component micro-frontend framework.

---

### DAY 56: Backend Memory & Connection Pool Tuning
- **Task Number**: 26
- **Title**: Backend Memory & Connection Pool Tuning
- **Priority**: P1
- **Why Needed**: Prevent memory leaks, excessive garbage collection pauses, and connection pool starvation during peak rental booking seasons.
- **Current State**: Default JVM settings and basic Hikari configuration without thread pool bounds or leak detection thresholds.
- **Expected Outcome**: Production-tuned HikariCP and JVM memory profiles with connection leak detection and GC telemetry.
- **Backend Work**:
  - Configure `leakDetectionThreshold=15000` (15s) in HikariCP settings.
  - Set container memory limits and G1GC parameters in `Dockerfile`.
  - Tune Spring thread pool task executors for asynchronous outbox and notification delivery.
- **Frontend Work**: None.
- **Database Work**: Configure PostgreSQL server parameters (`max_connections=100`, `shared_buffers=256MB`, `work_mem=16MB`).
- **Security Work**: Prevent DoS through slow query timeouts (`statement_timeout=30000`).
- **Tests**: Stress test verifying that abandoned unclosed connections are caught and logged by leak detector within 15 seconds.
- **Manual Verification**: Run load test; monitor Hikari active/idle connection graphs; verify connections return to pool promptly.
- **Documentation**: Update database tuning guide in `OPERATIONS.md`.
- **Success Criteria**: Zero connection leaks under load; Hikari connection acquisition wait time < 50ms.
- **Do Not Build**: Complex dynamic container autoscalers.

---

### DAY 57: Security Regression & Penetration Test
- **Task Number**: 27
- **Title**: Security Regression & Penetration Test
- **Priority**: P0 (Launch Blocker)
- **Why Needed**: Comprehensive verification that all security fixes (Days 31, 32, 33, 36, 37) are effective and have introduced zero regressions.
- **Current State**: Isolated security tests pass, but comprehensive regression suite across all attack vectors has not been run.
- **Expected Outcome**: Zero high or critical vulnerabilities across OWASP Top 10 categories (Injection, Broken Auth, Sensitive Data Exposure, XML/XXE, Broken Access Control, Security Misconfig, XSS, Insecure Deserialization, Vulnerable Components, Insufficient Logging).
- **Backend Work**: Patch any edge-case vulnerabilities identified during penetration testing.
- **Frontend Work**: Sanitize all dynamic HTML injections (`[innerHTML]`) with Angular `DomSanitizer`.
- **Database Work**: Ensure all database users have principle of least privilege in production.
- **Security Work**:
  - Execute automated OWASP ZAP or equivalent dynamic security scan against staging backend and frontend.
  - Attempt cross-tenant IDOR against every controller endpoint.
  - Test JWT token manipulation, forgery, and replay.
- **Tests**: Comprehensive automated security test suite (`SecurityRegressionSuiteTest`) verifying all authorization boundaries.
- **Manual Verification**: Manual penetration test using Burp Suite or OWASP ZAP; verify zero exploitable endpoints.
- **Documentation**: Publish formal Security Audit Summary in `SECURITY.md`.
- **Success Criteria**: Clean penetration test report with 0 Critical, 0 High, and 0 Medium vulnerabilities.
- **Do Not Build**: Theoretical post-quantum cryptography.

---

### DAY 58: Full Business E2E Regression
- **Task Number**: 28
- **Title**: Full Business E2E Regression
- **Priority**: P1
- **Why Needed**: Verify that security, pagination, and provider updates have not broken the core 22-step rental lifecycle.
- **Current State**: `Day30MasterE2EScenarioTest` passes with mock data in a single transactional test fixture.
- **Expected Outcome**: End-to-end integration test exercising actual HTTP endpoints across the entire lifecycle: Public Storefront -> AI Chat -> Availability -> Cart -> Checkout -> Lead -> Quote -> E-Signature -> Deposit -> Booking -> Warehouse Pick/Pack -> Dispatch -> Delivery -> Return -> Inspection -> Damage Billing -> Final Invoice -> Stripe Payment -> Financial Analytics.
- **Backend Work**: Ensure all service event listeners and outbox processors operate in integration test context.
- **Frontend Work**: Verify that complete workflow can be driven end-to-end via frontend Angular UI without errors.
- **Database Work**: Verify database state consistency after full cycle (inventory counts returned to stock, balances at zero).
- **Security Work**: Run E2E test under least-privileged role contexts (Customer, Warehouse Staff, Driver, Sales, Owner).
- **Tests**: Executable HTTP integration test (`FullBusinessLifecycleHttpE2ETest`) covering all 22 steps.
- **Manual Verification**: Execute the full demo script (`docs/demo-script.md`) manually in staging from storefront checkout to damage billing.
- **Documentation**: Update `docs/demo-script.md` with verified API payloads and expected outputs.
- **Success Criteria**: 100% of business lifecycle steps pass with strict financial and inventory ledger correctness.
- **Do Not Build**: Simulated multi-year accounting historical back-fill.

---

### DAY 59: Production Readiness Review & Sign-Off
- **Task Number**: 29
- **Title**: Production Readiness Review & Sign-Off
- **Priority**: P1
- **Why Needed**: Formal review against the production readiness checklist to ensure all operational, legal, and security requirements are satisfied.
- **Current State**: Checklists exist in `docs/production-checklist.md`, but many items were marked complete prematurely.
- **Expected Outcome**: Verified evidence for every item in the production checklist; formal sign-off by engineering and product leads.
- **Backend Work**: Final review of configuration toggles and environment variables.
- **Frontend Work**: Final verification of production builds and asset caching headers.
- **Database Work**: Verify production database migration version and backup retention settings.
- **Security Work**: Review all third-party API keys, secrets rotation procedures, and access control lists.
- **Tests**: Run complete regression test suite; verify 100% pass rate.
- **Manual Verification**: Walk through `docs/production-checklist.md` item by item, inspecting active configurations and running verification commands.
- **Documentation**: Finalize `docs/production-checklist.md` with signed verification dates and evidence links.
- **Success Criteria**: All P0 and P1 checklist items marked VERIFIED with documented evidence.
- **Do Not Build**: New features or speculative capabilities.

---

### DAY 60: Release Candidate & Launch Checklist
- **Task Number**: 30
- **Title**: Release Candidate & Launch Checklist
- **Priority**: P1
- **Why Needed**: Build and tag the definitive Release Candidate 1.0 (v1.0.0-rc1) artifacts and rehearse cutover.
- **Current State**: Development snapshot `backend-0.1.0-SNAPSHOT.jar`.
- **Expected Outcome**: Tagged release git commit, immutable Docker images published to private registry, and validated rollback rehearsal.
- **Backend Work**: Update `pom.xml` version to `1.0.0`.
- **Frontend Work**: Update `package.json` version to `1.0.0`.
- **Database Work**: Tag baseline Flyway schema version.
- **Security Work**: Final secret rotation; revoke development API keys and initialize production secrets.
- **Tests**: Final smoke test against tagged release candidate container image.
- **Manual Verification**: Execute dry-run rollback: deploy v1.0.0, simulate critical defect, execute rollback to previous image, verify database integrity.
- **Documentation**: Publish Release Notes and Launch Runbook in `DEPLOYMENT.md`.
- **Success Criteria**: Git tag `v1.0.0-rc1` created; production containers deployed and serving traffic; rollback verified.
- **Do Not Build**: Pre-release scope creep.

---

## 3. Capability Matrix (Actual Implementation State)

| Capability Area | Actual Implementation Status | Production Ready? | Primary Gaps / Required Remediation |
|---|---|---|---|
| **Tenant Isolation & Context** | BROKEN / MOCK_ONLY | **NO (P0 Blocker)** | Sourced from unverified headers; defaults to Evergreen Owner on empty request; cross-tenant IDOR |
| **Authentication & RBAC** | PARTIAL / BROKEN | **NO (P0 Blocker)** | No Spring Security; plaintext customer passwords; dummy portal tokens; missing password hashing |
| **Product Catalog** | COMPLETE | **YES** | Add database pagination on `GET /api/products` |
| **Customers & CRM Leads** | COMPLETE | **YES** | Add pagination; link guest checkouts idempotently |
| **Quotes & Calculation** | COMPLETE | **YES** | Verified mathematical calculations using BigDecimal |
| **Bookings & Lifecycle** | COMPLETE | **YES** | Add database unique constraint on `(tenant_id, quote_id)` |
| **Payments & Invoices** | PARTIAL | **NO (P0 Blocker)** | Lacks transaction reference idempotency; overpayment race conditions; manual entry only |
| **Real Payment Provider (Stripe)** | MOCK_ONLY | **NO (P2)** | Provider stub in `InboundWebhookService`; no real Stripe SDK or webhook signature check |
| **Customer Portal** | BROKEN / PARTIAL | **NO (P0 Blocker)** | Customer IDOR via `X-Customer-Id` header; plaintext password storage |
| **Notifications (In-App)** | COMPLETE | **YES** | Stored and delivered cleanly via event listeners |
| **Notifications (Email/SMS)** | MOCK_ONLY | **NO (P2)** | `MockEmailNotificationProvider` and `MockSmsNotificationProvider` (console only) |
| **Warehouse Fulfillment** | COMPLETE | **YES** | Pick, pack, and shortage workflow fully verified |
| **Inventory Availability Engine** | COMPLETE | **YES** | Canonical pessimistic write lock verified under concurrent load |
| **Hold Expiration** | NOT_IMPLEMENTED | **NO (P1)** | No background scheduler to expire temporary holds |
| **Delivery & Dispatch** | COMPLETE | **YES** | Route optimization is simple sort; manual schedule verified |
| **Returns & Inspection** | COMPLETE | **YES** | Return check-in, good/damaged/lost split verified |
| **Damage Claims & Billing** | COMPLETE | **YES** | Cost estimate, customer review, and invoice handoff verified |
| **Contracts & E-Sign** | NOT_IMPLEMENTED / MOCK | **NO (P2)** | Only a boolean flag on `Booking`; no document entity, hash, or real PDF generation |
| **PDF Generation** | NOT_IMPLEMENTED | **NO (P2)** | `InvoiceDocumentService` returns stub "NOT_IMPLEMENTED" |
| **Storefront & Public Cart** | COMPLETE | **YES** | Real-time availability revalidation verified; needs guest idempotency |
| **Checkout & Rental Request** | PARTIAL | **NO (P1)** | Missing DB unique constraint on idempotency key; duplicate guest customer creation |
| **Analytics & Reporting** | COMPLETE | **YES** | Authoritative calculations; needs query optimization for scale |
| **External Integrations & Outbox** | PARTIAL | **NO (P1)** | Outbox functions locally; connectors are mocks; lacks multi-instance distributed locking |
| **AI Sales Agent** | COMPLETE | **YES** | Role boundaries verified; cannot confirm bookings or take payments |
| **AI Copilot** | COMPLETE | **YES** | Internal staff only; role-enforced; cannot access foreign tenants |
| **AI Recommendations** | COMPLETE | **YES** | Signal detection, human approval, and revalidation verified |
| **Phone AI / Telephony** | MOCK_ONLY | **NO (P2)** | Mock provider only; no real SIP/Twilio trunk; turn-based HTTP demo |
| **Tax Engine** | MOCK_ONLY | **NO (P2)** | Hardcoded 8.25% demo tax; no multi-jurisdiction rules |
| **Document/Object Storage** | NOT_IMPLEMENTED | **NO (P2)** | No S3/MinIO; attachments not durable |
| **Observability & Metrics** | PARTIAL / BROKEN | **NO (P1)** | Health check exists; Actuator missing; no Prometheus metrics; no correlation IDs |
| **Backup & Disaster Recovery** | MOCK_ONLY / NOT_VERIFIED | **NO (P1)** | Markdown docs exist; no automated backup script or verified restore test |
| **CI/CD Pipeline** | NOT_IMPLEMENTED | **NO (P1)** | `.github` directory missing; no automated pipeline |
| **Frontend Container Build** | BROKEN | **NO (P0 Blocker)** | `frontend/Dockerfile` does not exist; Docker Compose build fails |

---

## 4. Production Readiness Scorecard

```text
================================================================================
RENTFLOW AI — PRODUCTION READINESS SCORECARD
================================================================================
Functional Completeness:            88 / 100
Security Readiness:                 32 / 100  (P0 Blocker: Plaintext auth, header IDOR, no JWT)
Reliability Readiness:              54 / 100  (P1 Gap: No distributed locks, payment race conditions)
Performance Readiness:              60 / 100  (P1 Gap: Missing pagination, unindexed queries)
Observability Readiness:            35 / 100  (P1 Gap: No Actuator, no Prometheus, unstructured logs)
Deployment Readiness:               30 / 100  (P0 Blocker: Missing frontend Dockerfile, no CI/CD)
Production Integration Readiness:   25 / 100  (P2 Gap: Email, SMS, Payment, Storage all mocks)
--------------------------------------------------------------------------------
OVERALL PRODUCTION READINESS:       46 / 100  (NOT READY FOR PRODUCTION LAUNCH)
================================================================================
```

### Scorecard Explanation & Evidence:
1. **Functional Completeness (88/100)**: The core 22-step rental domain lifecycle (catalog, pricing, quotes, bookings, warehouse, delivery, returns, inspection, damage, invoicing, AI sales, copilot, automations) is implemented in actual code and verified by 330 passing unit tests and master E2E tests. Gaps remain in real contracts, PDF rendering, and hold expirations.
2. **Security Readiness (32/100)**: Catastrophic vulnerabilities: authentication relies on forged HTTP headers; default fallback grants unauthenticated users OWNER access to Evergreen; customer passwords in portal are stored and compared in plaintext; portal users can access any customer's data via `X-Customer-Id`; wildcard CORS on payments.
3. **Reliability Readiness (54/100)**: Pessimistic inventory locking on bookings is well-implemented and passes concurrency tests. However, schedulers lack distributed locking, payment recording lacks idempotency deduplication, and database unique constraints are missing on quotes and idempotency keys.
4. **Performance Readiness (60/100)**: Controllers return unbounded entity lists without pagination; EAGER fetch relationships exist on several models; queries will degrade under production scale.
5. **Observability Readiness (35/100)**: Custom `HealthController` provides basic UP/READY status, but Spring Boot Actuator, Micrometer/Prometheus, correlation IDs, and JSON structured logging are absent.
6. **Deployment Readiness (30/100)**: `frontend/Dockerfile` is missing; `docker-compose.yml` fails on build; zero CI/CD workflows exist; no staging deployment exists.
7. **Production Integration Readiness (25/100)**: Payment (Stripe), email (SES), SMS (Twilio), telephony, accounting (QuickBooks), and object storage are entirely mock implementations or stubs.
