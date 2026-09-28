# Day 41 — Typed Production Configuration and Fail-Fast Validation

## Copy-Paste Coding Prompt

Act as a **Senior Java/Spring Boot Architect, Angular Architect, PostgreSQL Engineer, DevSecOps Engineer, and SaaS Production-Readiness Reviewer** working directly in the existing **RentFlow AI** repository:

`yarrabothusuresh/rentfllow-ai`

Do **not** rebuild the project. Inspect the current implementation first and preserve completed functionality.

## Objective

Make production configuration explicit, typed, validated, and fail-fast.

## Architecture Guardrails

- Keep the modular-monolith architecture.
- Backend: Java 17, Spring Boot, Spring Security, JPA/Hibernate, PostgreSQL, Flyway.
- Frontend: Angular 17.
- Tenant isolation is a hard security boundary.
- Derive identity, tenant, and roles only from trusted server-side authentication.
- Preserve financial decimal precision, booking/inventory concurrency safety, and payment/booking idempotency.
- AI must remain optional and must not block core rental workflows.
- Do not introduce Kafka, Kubernetes, microservices, CQRS, event sourcing, or service mesh unless explicitly required.
- Never commit real secrets, tokens, passwords, or customer data.

## Implementation Tasks

1. Create focused `@ConfigurationProperties` classes for JWT/security, AI, telephony, CORS, and other application-owned production settings.
2. Add Jakarta validation for required secrets, positive timeout/limit values, and safe origin configuration.
3. Ensure prod startup fails with clear actionable errors for missing/weak JWT secret or unsafe configuration.
4. Remove duplicate/conflicting property aliases where practical and document canonical environment variables.
5. Add unit tests for valid, missing, malformed, and boundary configuration values.

## Engineering Requirements

1. Inspect relevant code, tests, Flyway scripts, and docs before editing.
2. Use new Flyway migrations for schema changes.
3. Keep repositories/services tenant-scoped.
4. Keep controllers thin and business logic in appropriate services.
5. Validate untrusted input and fail closed for security-sensitive configuration.
6. Add tests for every material behavior change.
7. Do not disable existing tests to get a green build.
8. Update docs when configuration/deployment/operations behavior changes.
9. Preserve backward compatibility unless a documented breaking change is necessary.

## Required Verification

Run as applicable:

```bash
cd backend
mvn test
mvn verify

cd ../frontend
npm ci
npm test -- --watch=false
npm run build
```

Also run Docker/PostgreSQL/Flyway/security/staging checks required for this day.

## Acceptance Criteria

- [ ] Prod cannot start with empty/weak required secret.
- [ ] Configuration binds through typed properties.
- [ ] Invalid values fail with useful messages.
- [ ] Tests cover valid and invalid binding.
- [ ] Existing tenant-isolation/security tests remain green.
- [ ] No unsafe production fallback or real secret was introduced.
- [ ] Documentation matches actual behavior.

## Final Response Required from Coding Agent

Return: summary, files changed, architecture decisions, tests and results, commands run, security/tenant notes, remaining risks/TODOs, and recommended commit message.

Recommended commit:

```text
Day 41: typed production configuration and fail-fast validation
```
