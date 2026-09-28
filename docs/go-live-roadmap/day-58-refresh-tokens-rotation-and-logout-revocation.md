# Day 58 — Refresh Tokens Rotation and Logout Revocation

## Copy-Paste Coding Prompt

Act as a **Senior Java/Spring Boot Architect, Angular Architect, PostgreSQL Engineer, DevSecOps Engineer, and SaaS Production-Readiness Reviewer** working directly in `yarrabothusuresh/rentfllow-ai`.

Do **not** rebuild the project. Inspect the current implementation first and preserve completed functionality.

## Objective
Introduce secure long-lived sessions using short access tokens and server-tracked refresh tokens.

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
1. Use short-lived JWT access tokens.
2. Create hashed refresh-token/session persistence with expiry and device/session metadata.
3. Rotate refresh token on every successful refresh.
4. Detect reuse of previously rotated/revoked refresh token.
5. Implement logout-current-session and optional logout-all-sessions.
6. Add session listing/revocation support if appropriate for current UI.
7. Add tests for rotation, replay, expiry, revocation, and logout.

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
- [ ] Reused refresh token is rejected/detectable.
- [ ] Revoked session cannot refresh.
- [ ] Access token remains short-lived.
- [ ] Session lifecycle is auditable.
- [ ] Existing tenant-isolation/security tests remain green.
- [ ] No unsafe secret/default introduced.
- [ ] Docs match implementation.

## Final Response Required
Return summary, files changed, design decisions, tests/results, commands run, security notes, remaining risks, and recommended commit message.

```text
Day 58: refresh tokens rotation and logout revocation
```
