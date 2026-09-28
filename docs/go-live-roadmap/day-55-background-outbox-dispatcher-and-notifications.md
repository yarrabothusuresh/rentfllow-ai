# Day 55 — Background Outbox Dispatcher and Notifications

## Copy-Paste Coding Prompt

Act as a **Senior Java/Spring Boot Architect, Angular Architect, PostgreSQL Engineer, DevSecOps Engineer, and SaaS Production-Readiness Reviewer** working directly in `yarrabothusuresh/rentfllow-ai`.

Do **not** rebuild the project. Inspect the current implementation first and preserve completed functionality.

## Objective
Deliver outbox events reliably outside customer request latency.

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
1. Build scheduled/background dispatcher for pending outbox events.
2. Use safe claiming/locking semantics for multiple app instances.
3. Add bounded retries/backoff and terminal FAILED/dead-letter-like state.
4. Expose operational visibility into pending/retry/failed events.
5. Integrate at least one real use case such as booking confirmation notification/webhook.
6. Implement graceful shutdown and avoid duplicate processing.

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
- [ ] Concurrent workers do not double-process.
- [ ] Transient failures retry.
- [ ] Permanent failures visible/recoverable.
- [ ] Customer request latency is decoupled from notification delivery.
- [ ] Existing security/tenant tests remain green.
- [ ] No unsafe secret/default introduced.
- [ ] Docs match implementation.

## Final Response Required
Return summary, files changed, design decisions, tests/results, commands run, security notes, remaining risks, and recommended commit message.

```text
Day 55: background outbox dispatcher and notifications
```
