# RentFlow AI — Go-Live Roadmap Coding Prompts

Daily **copy-paste code-generation prompts for Days 40–70**, based on the senior architecture production-readiness review after Day 39.

## Roadmap Phases

- **Days 40–43 — Deployment & database correctness:** Docker/environment repair, typed configuration, Flyway bootstrap, PostgreSQL integration tests.
- **Days 44–47 — CI/CD:** backend CI, frontend CI, security scans, immutable container/staging delivery.
- **Days 48–51 — Observability:** Actuator, structured logging, Micrometer, SLOs/alerts.
- **Days 52–55 — Reliability:** outbound resilience, webhook idempotency, transactional outbox, background dispatcher.
- **Days 56–59 — Security lifecycle:** password reset, verification, refresh-token rotation, immutable audit trail.
- **Days 60–63 — Performance:** load harness, PostgreSQL query plans/indexes, JVM/pool tuning, regression gate.
- **Days 64–66 — Disaster recovery:** automated backups, real restore drill, rollback/migration safety.
- **Days 67–70 — Controlled production launch:** feature flags, smoke suite, first-customer readiness, final go-live gate.

## How to Use

Use **one file per development day**:

1. Pull the latest `main`.
2. Confirm the previous day's changes are merged and tests are green.
3. Copy the selected day's entire Markdown prompt into your coding agent.
4. Let the agent inspect the repository before changing code.
5. Review generated changes manually.
6. Run all verification required by that day's prompt.
7. Commit with the suggested Day number.
8. Merge only when acceptance criteria are satisfied.

Do not blindly apply future prompts if the repository has changed substantially. The prompt tells the coding agent to inspect current code and adapt without regressing completed work.

## Daily Prompt Files

- [Day 40 — Production Deployment Repair](day-40-production-deployment-repair.md)
- [Day 41 — Typed Production Configuration and Fail-Fast Validation](day-41-typed-production-configuration-and-fail-fast-validation.md)
- [Day 42 — PostgreSQL Clean Bootstrap and Flyway Validation](day-42-postgresql-clean-bootstrap-and-flyway-validation.md)
- [Day 43 — PostgreSQL Integration Test Foundation](day-43-postgresql-integration-test-foundation.md)
- [Day 44 — Backend CI Pipeline](day-44-backend-ci-pipeline.md)
- [Day 45 — Frontend CI Pipeline](day-45-frontend-ci-pipeline.md)
- [Day 46 — Security and Dependency Scanning Pipeline](day-46-security-and-dependency-scanning-pipeline.md)
- [Day 47 — Container Build Tagging and Staging Delivery](day-47-container-build-tagging-and-staging-delivery.md)
- [Day 48 — Actuator Health Liveness and Readiness](day-48-actuator-health-liveness-and-readiness.md)
- [Day 49 — Structured Logging and Correlation IDs](day-49-structured-logging-and-correlation-ids.md)
- [Day 50 — Micrometer and Business Metrics](day-50-micrometer-and-business-metrics.md)
- [Day 51 — Operational Alerts and SLO Baseline](day-51-operational-alerts-and-slo-baseline.md)
- [Day 52 — External API Resilience Layer](day-52-external-api-resilience-layer.md)
- [Day 53 — Webhook Reliability and Idempotency](day-53-webhook-reliability-and-idempotency.md)
- [Day 54 — Transactional Outbox Foundation](day-54-transactional-outbox-foundation.md)
- [Day 55 — Background Outbox Dispatcher and Notifications](day-55-background-outbox-dispatcher-and-notifications.md)
- [Day 56 — Password Reset Security Flow](day-56-password-reset-security-flow.md)
- [Day 57 — Email Verification and Account Activation](day-57-email-verification-and-account-activation.md)
- [Day 58 — Refresh Tokens Rotation and Logout Revocation](day-58-refresh-tokens-rotation-and-logout-revocation.md)
- [Day 59 — Immutable Business Audit Trail](day-59-immutable-business-audit-trail.md)
- [Day 60 — Performance Test Data and Load Test Harness](day-60-performance-test-data-and-load-test-harness.md)
- [Day 61 — Database Query Plan and Index Review](day-61-database-query-plan-and-index-review.md)
- [Day 62 — Connection Pool JVM and Resource Tuning](day-62-connection-pool-jvm-and-resource-tuning.md)
- [Day 63 — Performance Regression Gate](day-63-performance-regression-gate.md)
- [Day 64 — Automated PostgreSQL Backups](day-64-automated-postgresql-backups.md)
- [Day 65 — Restore Automation and Disaster Recovery Drill](day-65-restore-automation-and-disaster-recovery-drill.md)
- [Day 66 — Deployment Rollback and Migration Safety](day-66-deployment-rollback-and-migration-safety.md)
- [Day 67 — Feature Flags and Controlled Capability Rollout](day-67-feature-flags-and-controlled-capability-rollout.md)
- [Day 68 — Production Smoke and Critical Journey Suite](day-68-production-smoke-and-critical-journey-suite.md)
- [Day 69 — First Customer Operational Readiness](day-69-first-customer-operational-readiness.md)
- [Day 70 — Final Go-Live Gate and Release Candidate](day-70-final-go-live-gate-and-release-candidate.md)

## Architecture Direction

Continue with **Angular SPA → Spring Boot modular monolith → PostgreSQL** for initial go-live. Add operational components only where a measured requirement exists. The roadmap intentionally avoids premature microservices, Kafka, Kubernetes, service mesh, CQRS, and event sourcing.
