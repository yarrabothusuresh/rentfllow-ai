# Day 50 — Micrometer and Business Metrics

## Copy-Paste Coding Prompt

Act as a **Senior Java/Spring Boot Architect, Angular Architect, PostgreSQL Engineer, DevSecOps Engineer, and SaaS Production-Readiness Reviewer** working directly in `yarrabothusuresh/rentfllow-ai`.

Do **not** rebuild the project. Inspect the current implementation first and preserve completed functionality.

## Objective
Add low-cardinality technical and business metrics for operations.

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
1. Add Micrometer instrumentation for HTTP/JVM/HikariCP.
2. Create business metrics for booking create/fail, quote create/convert, inventory conflicts, payment success/failure, AI requests/errors/latency, and webhook failures.
3. Define one consistent metric naming convention.
4. Keep tags low-cardinality; avoid raw user/customer/tenant identifiers.
5. Document metric purpose and useful alert dimensions.

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
- [ ] Technical metrics emitted.
- [ ] Business success/failure metrics emitted.
- [ ] Latency measurable.
- [ ] Metric tags avoid unbounded cardinality.
- [ ] Existing security/tenant tests remain green.
- [ ] No unsafe secret/default introduced.
- [ ] Docs match implementation.

## Final Response Required
Return summary, files changed, design decisions, tests/results, commands run, security notes, remaining risks, and recommended commit message.

```text
Day 50: micrometer and business metrics
```
