# Day 61 — Database Query Plan and Index Review

## Copy-Paste Coding Prompt

Act as a **Senior Java/Spring Boot Architect, Angular Architect, PostgreSQL Engineer, DevSecOps Engineer, and SaaS Production-Readiness Reviewer** working directly in `yarrabothusuresh/rentfllow-ai`.

Do **not** rebuild the project. Inspect the current implementation first and preserve completed functionality.

## Objective
Validate PostgreSQL query plans and indexes using realistic data volume.

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
1. Identify high-frequency/slow queries for availability, booking listing/search, customer search, quotes, invoices, and dashboards.
2. Run `EXPLAIN (ANALYZE, BUFFERS)` on realistic staging data.
3. Verify Day 39 composite indexes are actually used.
4. Fix N+1 queries, avoidable sequential scans, inefficient count queries, and over-fetching.
5. Add/remove indexes only with measurable evidence and Flyway migration.
6. Document before/after plans and timings.

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
- [ ] Critical query plans evidenced.
- [ ] Avoidable N+1/full scans addressed.
- [ ] Index changes are justified by measurements.
- [ ] Schema changes use Flyway.
- [ ] Existing tenant-isolation/security tests remain green.
- [ ] No unsafe secret/default introduced.
- [ ] Docs match implementation.

## Final Response Required
Return summary, files changed, design decisions, tests/results, commands run, security notes, remaining risks, and recommended commit message.

```text
Day 61: database query plan and index review
```
