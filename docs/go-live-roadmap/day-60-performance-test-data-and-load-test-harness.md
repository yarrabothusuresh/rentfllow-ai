# Day 60 — Performance Test Data and Load Test Harness

## Copy-Paste Coding Prompt

Act as a **Senior Java/Spring Boot Architect, Angular Architect, PostgreSQL Engineer, DevSecOps Engineer, and SaaS Production-Readiness Reviewer** working directly in `yarrabothusuresh/rentfllow-ai`.

Do **not** rebuild the project. Inspect the current implementation first and preserve completed functionality.

## Objective
Create realistic staging data and repeatable load tests for critical APIs.

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
1. Build synthetic data generator for multiple tenants, products, customers, quotes, bookings, inventory reservations, invoices, and payments.
2. Use k6, Gatling, or JMeter for repeatable load testing.
3. Model realistic read/write traffic rather than only maximum throughput.
4. Measure p50/p95/p99 latency, throughput, and error rate.
5. Create staging-only defaults and prevent accidental production destructive load tests.
6. Store baseline results for later comparison.

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

Run additional security/data checks required for this day.

## Acceptance Criteria
- [ ] Synthetic dataset reproducible.
- [ ] No real PII used.
- [ ] Critical APIs have latency/error baselines.
- [ ] Load tooling is safe by default.
- [ ] Existing tenant-isolation/security tests remain green.
- [ ] No unsafe secret/default introduced.
- [ ] Docs match implementation.

## Final Response Required
Return summary, files changed, design decisions, tests/results, commands run, security notes, remaining risks, and recommended commit message.

```text
Day 60: performance test data and load test harness
```
