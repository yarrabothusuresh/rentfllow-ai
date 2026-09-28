# Day 67 — Feature Flags and Controlled Capability Rollout

## Copy-Paste Coding Prompt

Act as a **Senior Java/Spring Boot Architect, Angular Architect, PostgreSQL Engineer, DevSecOps Engineer, and SaaS Production-Readiness Reviewer** working directly in `yarrabothusuresh/rentfllow-ai`.

Do **not** rebuild the project. Inspect the current implementation first and preserve completed functionality.

## Objective
Allow risky/optional capabilities to be released gradually without changing core transactional behavior.

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
1. Implement a lightweight configuration/database-backed feature flag mechanism appropriate for a modular monolith.
2. Support independent flags for AI Sales Agent, AI Copilot, Phone AI, automations, and experimental integrations.
3. Support environment-level flags and tenant-level overrides only where needed.
4. Default optional/high-risk features OFF for new production tenants.
5. Ensure core catalog/inventory/quote/booking/payment/warehouse/returns workflows work when all AI flags are off.
6. Audit privileged feature-flag changes.

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
- [ ] Core workflows work with AI flags off.
- [ ] Tenant-specific flags are isolated.
- [ ] Risky features default off for new tenants.
- [ ] Flag changes are auditable.
- [ ] Existing tenant-isolation/security tests remain green.
- [ ] No unsafe secret/default introduced.
- [ ] Docs match implementation.

## Final Response Required
Return summary, files changed, design decisions, tests/results, commands run, security notes, remaining risks, and recommended commit message.

```text
Day 67: feature flags and controlled capability rollout
```
