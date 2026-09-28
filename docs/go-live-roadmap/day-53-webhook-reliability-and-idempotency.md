# Day 53 — Webhook Reliability and Idempotency

## Copy-Paste Coding Prompt

Act as a **Senior Java/Spring Boot Architect, Angular Architect, PostgreSQL Engineer, DevSecOps Engineer, and SaaS Production-Readiness Reviewer** working directly in `yarrabothusuresh/rentfllow-ai`.

Do **not** rebuild the project. Inspect the current implementation first and preserve completed functionality.

## Objective
Make inbound payment/telephony/integration webhooks secure, replay-resistant, and duplicate-safe.

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
1. Verify provider signatures using constant-time-safe patterns where applicable.
2. Add replay/timestamp tolerance protection when provider supports signed timestamps.
3. Persist webhook receipt, provider event ID, processing status, attempts, and timestamps for important providers.
4. Make handlers idempotent so duplicate delivery cannot double-charge or double-transition state.
5. Return provider-appropriate HTTP codes without leaking internals.
6. Add invalid-signature, replay, duplicate, and retry tests.

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
- [ ] Invalid signatures rejected.
- [ ] Duplicates are harmless.
- [ ] Processing history is visible.
- [ ] Provider retries do not corrupt business state.
- [ ] Existing security/tenant tests remain green.
- [ ] No unsafe secret/default introduced.
- [ ] Docs match implementation.

## Final Response Required
Return summary, files changed, design decisions, tests/results, commands run, security notes, remaining risks, and recommended commit message.

```text
Day 53: webhook reliability and idempotency
```
