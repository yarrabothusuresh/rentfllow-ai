# Day 66 — Deployment Rollback and Migration Safety

## Copy-Paste Coding Prompt

Act as a **Senior Java/Spring Boot Architect, Angular Architect, PostgreSQL Engineer, DevSecOps Engineer, and SaaS Production-Readiness Reviewer** working directly in `yarrabothusuresh/rentfllow-ai`.

Do **not** rebuild the project. Inspect the current implementation first and preserve completed functionality.

## Objective
Make application rollback and database evolution safe and operationally realistic.

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
1. Replace any unsupported rollback commands in docs with commands supported by the chosen platform.
2. Use immutable image versions/tags for rollback.
3. Define Flyway expand-and-contract compatibility rules.
4. Do not recommend deleting rows from `flyway_schema_history` as a normal production rollback strategy.
5. Document when rollback is safe and when roll-forward is required.
6. Practice one application rollback in staging and capture results.

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
- [ ] Rollback command is tested.
- [ ] Prior application image can be redeployed.
- [ ] Migration policy avoids unsafe history manipulation.
- [ ] Rollback vs roll-forward decision is documented.
- [ ] Existing tenant-isolation/security tests remain green.
- [ ] No unsafe secret/default introduced.
- [ ] Docs match implementation.

## Final Response Required
Return summary, files changed, design decisions, tests/results, commands run, security notes, remaining risks, and recommended commit message.

```text
Day 66: deployment rollback and migration safety
```
