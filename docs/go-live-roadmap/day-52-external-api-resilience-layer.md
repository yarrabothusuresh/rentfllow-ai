# Day 52 — External API Resilience Layer

## Copy-Paste Coding Prompt

Act as a **Senior Java/Spring Boot Architect, Angular Architect, PostgreSQL Engineer, DevSecOps Engineer, and SaaS Production-Readiness Reviewer** working directly in `yarrabothusuresh/rentfllow-ai`.

Do **not** rebuild the project. Inspect the current implementation first and preserve completed functionality.

## Objective
Prevent AI, payment, telephony, email/SMS, or other providers from destabilizing core rental flows.

## Architecture Guardrails
- Keep the modular monolith.
- Java 17 + Spring Boot + Spring Security + JPA/Hibernate + PostgreSQL + Flyway.
- Angular 17 frontend.
- Tenant isolation and server-verified identity are mandatory.
- Preserve financial precision, concurrency safety, and idempotency.
- AI is optional and must not block core rental workflows.
- No new microservices/Kafka/Kubernetes/CQRS/event sourcing unless explicitly required.
- Never commit real secrets or customer data.

## Implementation Tasks
1. Standardize connect/read timeouts for outbound clients.
2. Add bounded retry with exponential backoff only for safe/retryable operations.
3. Add circuit breaker behavior for unstable providers.
4. Classify retryable, permanent, validation, and rate-limit failures.
5. Do not blindly retry non-idempotent payment or mutation calls.
6. Add metrics/tests for timeout, 429, 5xx, circuit-open, and recovery.
7. Ensure AI unavailability never blocks catalog/quote/booking/payment/warehouse/returns.

## Engineering Requirements
1. Inspect code/tests/docs first.
2. Use Flyway for DB changes.
3. Keep persistence tenant-scoped.
4. Keep controllers thin.
5. Validate untrusted input and fail closed.
6. Add automated tests for material changes.
7. Do not disable existing tests.
8. Update operational docs when behavior changes.

## Verification
```bash
cd backend
mvn test
mvn verify

cd ../frontend
npm ci
npm test -- --watch=false
npm run build
```

Run any additional operational/security checks required.

## Acceptance Criteria
- [ ] No indefinite external waits.
- [ ] Retries bounded and safe.
- [ ] Circuit behavior observable.
- [ ] Core non-AI flows survive AI outage.
- [ ] Existing security/tenant tests remain green.
- [ ] No unsafe secret/default introduced.
- [ ] Docs match implementation.

## Final Response Required
Return summary, files changed, design decisions, tests/results, commands run, security notes, remaining risks, and recommended commit message.

```text
Day 52: external api resilience layer
```
