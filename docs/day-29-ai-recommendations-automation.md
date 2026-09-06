# Day 29 — RentFlow AI Recommendations & Safe Business Automation

## Executive Summary

RentFlow AI Day 29 implements **AI Recommendations & Safe Business Automation** — evolving RentFlow from a reactive assistant into a proactive, autonomous business operating system.

Instead of waiting for a user to query the system or check dashboards, RentFlow continuously analyzes production records to detect operational bottlenecks, financial risks, and revenue opportunities. It provides explainable AI recommendations backed by non-LLM authoritative facts, submits safe operational actions to a human approval queue, and enforces pre-execution revalidation to prevent stale or conflicting changes.

```
+----------------------------------------------------------------------------------------------------+
|                   RENTFLOW AI PROACTIVE RECOMMENDATIONS & AUTOMATION ARCHITECTURE                  |
+----------------------------------------------------------------------------------------------------+
|                                                                                                    |
|    [ SCHEDULE / EVENT ]                                                                            |
|             │                                                                                      |
|             ▼                                                                                      |
|  +─────────────────────────────────────────+                                                       |
|  |       Deterministic Signal Detectors    |  • QuoteSignalDetector (Expiring, Low Margin)         |
|  |        (100% Non-LLM Authority)         |  • ProfitabilitySignalDetector (Negative Margin)       |
|  +────────────────────┬────────────────────+  • DeliverySignalDetector (Missing Driver, At Risk)   |
|                       │                       • WarehouseSignalDetector (Shortages, Unassigned)    |
|                       │                       • FinanceSignalDetector (Overdue Invoices, Deposits) |
|                       │                       • CrmSignalDetector (Overdue Follow-ups, High Value) |
|                       │                       • ClaimsAndMaintenanceSignalDetector                 |
|                       │                       • IntegrationSignalDetector (Dead-Letter Events)     |
|                       ▼                                                                            |
|  +─────────────────────────────────────────+                                                       |
|  |          BusinessSignalService          |                                                       |
|  | • Deduplication (dedupeKey per entity)  |                                                       |
|  | • Lifecycle & Auto-Resolution Tracking  |                                                       |
|  +────────────────────┬────────────────────+                                                       |
|                       │                                                                            |
|                       ▼                                                                            |
|  +─────────────────────────────────────────+                                                       |
|  |     Deterministic Automation Rules      |  Rule Evaluation & Cooldown Check:                    |
|  |  • RECOMMEND_ONLY                       |  • Allowed Roles Filter                               |
|  |  • APPROVAL_REQUIRED                    |  • Max Executions Per Day                             |
|  |  • AUTO_EXECUTE_LOW_RISK                |  • Cooldown Period (e.g. 60m)                         |
|  +────────────────────┬────────────────────+                                                       |
|                       │                                                                            |
|                       ▼                                                                            |
|  +─────────────────────────────────────────+                                                       |
|  |      RecommendationExplanationService   |  • AI Explanation (Claude / OpenAI / Local)           |
|  |      (AI Narrative + Offline Fallback)  |  • Deterministic Offline Fallback Mode                |
|  +────────────────────┬────────────────────+                                                       |
|                       │                                                                            |
|                       ▼                                                                            |
|  +─────────────────────────────────────────+                                                       |
|  |          RecommendationService          |                                                       |
|  | • Sequential REC-000001 Numbering       |                                                       |
|  | • Evidence Snapshot & Strength          |                                                       |
|  | • Status (NEW, REVIEWED, RESOLVED, ...) |                                                       |
|  +────────────────────┬────────────────────+                                                       |
|                       │                                                                            |
|         ┌─────────────┴────────────────────────┐                                                   |
|         ▼ (High/Medium Risk or Mode)           ▼ (Low Risk & AUTO_EXECUTE)                         |
|  +───────────────────────────────+   +──────────────────────────────────+                          |
|  |    AutomationApprovalService  |   |    AutomationExecutionService    |                          |
|  |  • Human Review Queue         |   |                                  |                          |
|  |  • Role-Based Confirmation    |──>|  1. Idempotency Key Guard        |                          |
|  |  • Rejection with Reason      |   |  2. Pre-Execution Revalidation   |                          |
|  +───────────────────────────────+   |  3. Safe Action Handler Execute  |                          |
|                                      |  4. Status: EXECUTED/SKIPPED/STALE|                         |
|                                      |  5. Audit Trail & Outcome Record |                          |
|                                      +──────────────────────────────────+                          |
+----------------------------------------------------------------------------------------------------+
```

---

## Key Design Principles & Safety Boundaries

### 1. The Non-Authoritative Rule
- **The LLM is NEVER the source of truth** for financial calculations, margins, delivery schedules, inventory counts, or contract execution.
- All 15+ business situations are detected **deterministically** by pure Java detector components querying database repositories.
- The AI only provides natural-language narrative synthesis ("Why this matters", "Context & Impact") and never determines whether a signal exists.
- If AI services are unavailable or disabled (`AI_PROVIDER=mock`), deterministic fallback explanations ensure uninterrupted operation.

### 2. Guarded Action Boundaries (Strictly Prohibited Actions)
The automation engine enforces a strict safety perimeter. The following high-impact operations are **categorically prohibited** from autonomous execution:
- ❌ Direct refunds or payment captures
- ❌ Order or booking cancellations
- ❌ Contract price overrides or discount modifications
- ❌ Inventory write-offs or disposals
- ❌ Account terminations or customer disqualifications

### 3. Allowed Safe Action Handlers
Only explicitly defined, idempotent, low/medium-risk handlers are permitted:
1. `CREATE_INTERNAL_TASK`: Generates high-priority review tasks for operational leads.
2. `SEND_QUOTE_REMINDER`: Sends follow-up reminders to customers with expiring quotes.
3. `SEND_INVOICE_REMINDER`: Triggers payment reminder notifications for overdue invoices.
4. `SEND_DEPOSIT_REMINDER`: Requests required event deposits within target fulfillment windows.
5. `SEND_CONTRACT_REMINDER`: Urges signature on pending rental agreements.
6. `ASSIGN_DRIVER`: Assigns available, active fleet drivers to unassigned delivery dispatches.
7. `ASSIGN_WAREHOUSE_OPERATOR`: Assigns warehouse fulfillment staff to pending pick orders.
8. `CREATE_LEAD_FOLLOW_UP`: Creates structured CRM follow-up activities for untouched leads.
9. `RETRY_INTEGRATION_EVENT`: Re-queues dead-lettered webhook / API events with exponential backoff.
10. `CREATE_DAMAGE_REVIEW_TASK`: Alerts claim specialists to open, unestimated damage claims.
11. `CREATE_MAINTENANCE_REVIEW_TASK`: Schedules preventative inspection for equipment under maintenance.

### 4. Pre-Execution Revalidation
Before any action handler executes:
- The handler verifies if the entity still exists and matches the expected state.
- **Already Completed Check**: If another user already assigned the driver or the invoice was already paid, the action is marked `SKIPPED` (not duplicated).
- **Stale Check**: If the quote expired or was cancelled, execution is aborted with `STALE` status.

### 5. Multi-Tenant Isolation & Role Security
- All signals, recommendations, rules, approvals, and execution logs are partitioned by `tenantId`.
- **Customer Role Block**: Any request from a `CUSTOMER` role to access recommendations, trigger detection, or confirm an internal approval is rejected immediately with `403 Forbidden` / `SecurityException`.
- Approvals require appropriate roles (`ADMIN`, `MANAGER`, `SALES`, `FINANCE`, `WAREHOUSE_MANAGER`).

---

## REST Endpoints Reference

### 1. Recommendations API (`/api/recommendations`)
- `GET /api/recommendations` — List recommendations with optional status, priority, and category filters.
- `GET /api/recommendations/{id}` — Get detailed recommendation with authoritative evidence snapshot.
- `POST /api/recommendations/{id}/review` — Mark recommendation as reviewed by employee.
- `POST /api/recommendations/{id}/dismiss` — Dismiss recommendation with required dismissal reason.
- `POST /api/recommendations/{id}/propose-action` — Propose suggested action and route to human approval queue.
- `POST /api/recommendations/detect` — Trigger on-demand proactive detection scan across all detectors.

### 2. Automation Engine API (`/api/automation`)
- `GET /api/automation/dashboard` — KPI metrics, pending approvals count, and recent activity queues.
- `GET /api/automation/signals` — View active, deduplicated business signals with evidence.
- `GET /api/automation/approvals` — List approval requests with optional status filter (`PENDING`, `APPROVED`, `REJECTED`).
- `POST /api/automation/approvals/{id}/approve` — Confirm and execute queued action.
- `POST /api/automation/approvals/{id}/reject` — Reject proposed action with explanation note.
- `GET /api/automation/rules` — View registered automation policies and execution modes.
- `PUT /api/automation/rules/{id}/mode` — Switch mode (`RECOMMEND_ONLY`, `APPROVAL_REQUIRED`, `AUTO_EXECUTE_LOW_RISK`).
- `POST /api/automation/rules/{id}/toggle` — Enable or disable specific automation rules.
- `GET /api/automation/executions` — View chronological execution audit logs.

---

## Verification & Test Suite Summary

The Day 29 test suite verifies the complete pipeline under automated test runners:
- **`Day29SignalDetectionTest`**:
  - `testDetectExpiringQuoteSignal`: Detects quote expiring within 48 hours, verifies deduplication key, and confirms non-LLM authoritative evidence collection.
  - `testDetectOverdueInvoiceAndUnpaidDeposit`: Detects overdue accounts receivable invoices and upcoming bookings with unpaid deposits.
  - `testSignalDeduplicationAndUpdate`: Verifies that subsequent runs update existing signals rather than spamming duplicate records.
- **`Day29RecommendationAndExplanationTest`**:
  - `testRecommendationCreationAndFormatting`: Verifies `REC-000001` sequential formatting, evidence strength, and deterministic fallback explanation generation.
  - `testRecommendationReviewAndDismissal`: Tests status transitions (`NEW` → `REVIEWED` → `DISMISSED`) and audit metadata.
- **`Day29AutomationApprovalAndExecutionTest`**:
  - `testHumanApprovalAndExecutionFlow`: Validates human-in-the-loop gate, blocks customer role execution, confirms manager approval, executes delivery assignment in DB, and resolves recommendation.
  - `testPreExecutionRevalidationDetectsStaleEntity`: Validates that already-completed entities are safely skipped without duplicate execution.
  - `testAutoExecuteLowRiskMode`: Verifies autonomous execution of internal task creation when mode is set to `AUTO_EXECUTE_LOW_RISK`.
