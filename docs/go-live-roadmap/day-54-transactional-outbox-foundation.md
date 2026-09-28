# Day 54 — Transactional Outbox Foundation

## Copy-Paste Coding Prompt

Act as a **Senior Java/Spring Boot Architect, Angular Architect, PostgreSQL Engineer, DevSecOps Engineer, and SaaS Production-Readiness Reviewer** working directly in `yarrabothusuresh/rentfllow-ai`.

Do **not** rebuild the project. Inspect the current implementation first and preserve completed functionality.

## Objective
Persist reliable business events atomically with core transactions without adding Kafka.

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
1. Add Flyway migration for `outbox_event` with event ID, tenant ID, aggregate type/id, event type, payload, status, attempts, next-attempt time, timestamps, and version metadata.
2. Create domain/repository/service abstractions.
3. Write outbox record inside the same DB transaction as the business state mutation.
4. Keep event payload minimal and avoid unnecessary PII.
5. Add commit/rollback atomicity tests and unique event ID constraints.

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
- [ ] Business change and event commit atomically.
- [ ] Rollback leaves no orphan event.
- [ ] Schema managed by Flyway.
- [ ] Event identity supports idempotency.
- [ ] Existing security/tenant tests remain green.
- [ ] No unsafe secret/default introduced.
- [ ] Docs match implementation.

## Final Response Required
Return summary, files changed, design decisions, tests/results, commands run, security notes, remaining risks, and recommended commit message.

```text
Day 54: transactional outbox foundation
```
