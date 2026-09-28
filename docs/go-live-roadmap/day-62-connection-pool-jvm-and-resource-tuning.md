# Day 62 — Connection Pool JVM and Resource Tuning

## Copy-Paste Coding Prompt

Act as a **Senior Java/Spring Boot Architect, Angular Architect, PostgreSQL Engineer, DevSecOps Engineer, and SaaS Production-Readiness Reviewer** working directly in `yarrabothusuresh/rentfllow-ai`.

Do **not** rebuild the project. Inspect the current implementation first and preserve completed functionality.

## Objective
Tune HikariCP, server threads, timeouts, and JVM/container resources using measured load.

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
1. Measure Hikari pool usage and tune max/min/timeout settings for target concurrency.
2. Review Tomcat request threads, connection/request timeouts, and upload/request limits.
3. Review JVM/container memory, MaxRAMPercentage, GC behavior, and resource limits.
4. Align platform container CPU/memory limits with JVM settings.
5. Keep environment-specific tuning configurable.
6. Document staging measurements and rationale.

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
- [ ] No DB pool starvation at target load.
- [ ] Application stays inside memory limits.
- [ ] Timeouts fail predictably instead of hanging.
- [ ] Tuning values are configurable.
- [ ] Existing tenant-isolation/security tests remain green.
- [ ] No unsafe secret/default introduced.
- [ ] Docs match implementation.

## Final Response Required
Return summary, files changed, design decisions, tests/results, commands run, security notes, remaining risks, and recommended commit message.

```text
Day 62: connection pool jvm and resource tuning
```
