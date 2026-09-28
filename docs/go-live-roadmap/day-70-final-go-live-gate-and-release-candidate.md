# Day 70 — Final Go-Live Gate and Release Candidate

## Copy-Paste Coding Prompt

Act as a **Senior Java/Spring Boot Architect, Angular Architect, PostgreSQL Engineer, DevSecOps Engineer, and SaaS Production-Readiness Reviewer** working directly in `yarrabothusuresh/rentfllow-ai`.

Do **not** rebuild the project. Inspect the current implementation first and preserve completed functionality.

## Objective
Perform an evidence-based final readiness review and prepare the first controlled production release.

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
1. Run backend CI, frontend CI, security scans, PostgreSQL integration tests, Flyway validation, staging deployment, smoke suite, and performance baseline.
2. Review tenant isolation, RBAC/IDOR, concurrency, idempotency, payment/webhook reliability, auditability, and observability.
3. Verify backup/restore drill and application rollback evidence.
4. Verify AI/provider outage does not block core rental operations.
5. Update README, production checklist, deployment docs, and operations docs so they reflect actual repository behavior.
6. Create a go-live sign-off document with passed gates, known accepted risks, owners, rollback point, and post-launch monitoring plan.
7. Create release candidate tag/version only after mandatory gates are green.

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

Run additional DR/release/staging checks required for this day.

## Acceptance Criteria
- [ ] All P0 go-live blockers are closed.
- [ ] Staging deploy from immutable artifacts passes smoke tests.
- [ ] Restore and rollback have been exercised.
- [ ] AI outage preserves core rental flows.
- [ ] Release documentation matches actual behavior.
- [ ] Existing tenant-isolation/security tests remain green.
- [ ] No unsafe secret/default introduced.
- [ ] Docs match implementation.

## Final Response Required
Return summary, files changed, design decisions, tests/results, commands run, security notes, remaining risks, and recommended commit message.

```text
Day 70: final go-live gate and release candidate
```
