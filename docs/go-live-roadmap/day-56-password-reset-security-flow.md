# Day 56 — Password Reset Security Flow

## Copy-Paste Coding Prompt

Act as a **Senior Java/Spring Boot Architect, Angular Architect, PostgreSQL Engineer, DevSecOps Engineer, and SaaS Production-Readiness Reviewer** working directly in `yarrabothusuresh/rentfllow-ai`.

Do **not** rebuild the project. Inspect the current implementation first and preserve completed functionality.

## Objective
Implement production-safe password reset for staff and customer identities.

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
1. Add forgot-password endpoint with a generic response that does not reveal account existence.
2. Generate cryptographically random single-use reset tokens.
3. Store only a secure hash of reset tokens with expiration and usage status.
4. Add reset-password endpoint with password policy validation.
5. Invalidate token after use and invalidate relevant sessions where appropriate.
6. Rate-limit reset requests and record security audit events.
7. Add tests for expired, reused, malformed, and cross-account tokens.

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
- [ ] Reset tokens are single-use and expiring.
- [ ] Account enumeration is prevented.
- [ ] Passwords/tokens never logged or emailed in plaintext.
- [ ] Abuse controls exist.
- [ ] Existing tenant-isolation/security tests remain green.
- [ ] No unsafe secret/default introduced.
- [ ] Docs match implementation.

## Final Response Required
Return summary, files changed, design decisions, tests/results, commands run, security notes, remaining risks, and recommended commit message.

```text
Day 56: password reset security flow
```
