# Day 69 — First Customer Operational Readiness

## Copy-Paste Coding Prompt

Act as a **Senior Java/Spring Boot Architect, Angular Architect, PostgreSQL Engineer, DevSecOps Engineer, and SaaS Production-Readiness Reviewer** working directly in `yarrabothusuresh/rentfllow-ai`.

Do **not** rebuild the project. Inspect the current implementation first and preserve completed functionality.

## Objective
Make the first production tenant onboarding and support process repeatable.

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
1. Create first-tenant provisioning checklist.
2. Cover owner/admin account, roles, catalog import, starting inventory, pricing/tax, payment setup, email sender/domain, backup verification, and support contacts.
3. Create data import validation plus rollback rules.
4. Create support triage guides for login, inventory mismatch, booking conflict, payment issue, and failed notification.
5. Ensure demo users/data/credentials cannot be seeded into real production tenants.
6. Define onboarding verification/sign-off.

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
- [ ] First tenant can be provisioned repeatably.
- [ ] Demo data is blocked in production.
- [ ] Support triage has concrete first actions.
- [ ] Onboarding includes verification/sign-off.
- [ ] Existing tenant-isolation/security tests remain green.
- [ ] No unsafe secret/default introduced.
- [ ] Docs match implementation.

## Final Response Required
Return summary, files changed, design decisions, tests/results, commands run, security notes, remaining risks, and recommended commit message.

```text
Day 69: first customer operational readiness
```
