# Day 28 — RentFlow AI Copilot: Internal Natural-Language Business Assistant

## Executive Summary

RentFlow AI Day 28 implements the internal **RentFlow AI Copilot** — an enterprise business assistant designed for RentFlow employees (Business Owners, Sales Representatives, Operations Managers, Warehouse Leads, and Finance Officers). 

Building upon the AI Sales infrastructure established in Day 27, the Copilot enables authorized employees to query operational health, analyze margin changes, uncover fulfillment bottlenecks, calculate accounts receivable aging, search customer records with disambiguation, and propose safe operational actions with explicit human confirmation.

```
+------------------------------------------------------------------------------------------------+
|                             RENTFLOW AI COPILOT ARCHITECTURE                                   |
+------------------------------------------------------------------------------------------------+
|                                                                                                |
|   [ Owner / Exec ]     [ Sales Rep ]     [ Operations Mgr ]     [ Warehouse ]     [ Finance ]  |
|          │                   │                   │                    │                │       |
|          └───────────────────┴─────────────┬─────┴────────────────────┴────────────────┘       |
|                                            │ Role-Based Request (JWT / RBAC Headers)           |
|                                            ▼                                                   |
|                        +───────────────────────────────────────+                               |
|                        |          CopilotController            |                               |
|                        |   /api/copilot/conversations          |                               |
|                        |   /api/copilot/actions/{id}/confirm   |                               |
|                        +───────────────────┬───────────────────+                               |
|                                            │                                                   |
|                                            ▼                                                   |
|                        +───────────────────────────────────────+                               |
|                        |          AiCopilotService             |                               |
|                        +─────┬───────────────────────────┬─────+                               |
|                              │                           │                                     |
|             ┌────────────────┴───────────────┐           │                                     |
|             ▼                                ▼           ▼                                     |
|  +───────────────────────+       +───────────────────────────────────+                         |
|  | CopilotBriefingService|       |      Security & Guardrails        |                         |
|  | • Daily Morning Brief |       | • Customer Role Forbidden (403)   |                         |
|  | • Role-Tailored KPIs  |       | • Prompt Injection Defense        |                         |
|  | • Attention Highlights|       | • Strict Multi-Tenant Isolation   |                         |
|  +───────────────────────+       | • Prohibited Action Interceptor   |                         |
|                                  +─────────────────┬─────────────────+                         |
|                                                    │                                           |
|                                                    ▼                                           |
|                                  +───────────────────────────────────+                         |
|                                  |       AiSalesToolRegistry         |                         |
|                                  +─────────────────┬─────────────────+                         |
|                                                    │                                           |
|         ┌─────────────────┬────────────────┬───────┴────────┬────────────────┬──────────────┐  |
|         ▼                 ▼                ▼                ▼                ▼              ▼  |
|  [GetExecutive]   [GetMarginTrend]  [GetAttention]   [GetWarehouse]   [GetArAging]   [Propose] |
|   Analytics        Cost Drivers       Signals          Blockers        30+ Days       Action   |
|     Service          Service          Service          Service         Invoices       Service  |
|                                                                                             │  |
|                                                                                             ▼  |
|                                                                                +──────────────+|
|                                                                                | Human Review ||
|                                                                                |  & Confirm   ||
|                                                                                +──────────────+|
+------------------------------------------------------------------------------------------------+
```

---

## 1. Core Design Principles

### Principle 1: The Non-Authoritative Rule
The LLM is **never the source of truth** for financial figures, margins, product availability, booking states, or overdue balances. All numbers and operational statuses are computed deterministically by authoritative Spring Boot domain services (`AnalyticsService`, `ProfitabilityService`, `BookingAttentionService`, `InvoiceRepository`, `DeliveryService`, `WarehouseService`).

### Principle 2: Safe Human-in-the-Loop Action Mutations
The Copilot is prohibited from autonomously mutating critical business state. When an employee asks to perform an action (such as assigning a driver to a delivery or scheduling a lead follow-up):
1. Copilot prepares a server-side `CopilotActionProposal` with status `PROPOSED`.
2. The proposal is rendered in the UI with an explicit confirmation card displaying the target entity, parameter diff, and risk level.
3. The mutation only executes when the employee clicks **Confirm & Execute** via `POST /api/copilot/actions/{proposalId}/confirm`.
4. The server independently re-validates the user's role permissions, tenant boundaries, and target entity validity prior to invoking domain logic.

### Principle 3: Prohibited High-Impact Operations
Certain actions carry catastrophic risk and are **strictly forbidden** from Copilot proposals:
* Issuing refunds or charging payment cards
* Cancelling confirmed bookings or contracts
* Manual price overrides or discount inflation
* Writing off inventory quantities without physical count audits

When requested, the Copilot explains why the operation is prohibited and directs the employee to the standard administrative workflow.

### Principle 4: Strict Multi-Tenant Isolation & Role Authorization
* **Customer Prohibition**: The `CUSTOMER` role is strictly forbidden (`403 Forbidden`). Only internal employee roles (`OWNER`, `ADMIN`, `SALES`, `OPERATIONS`, `WAREHOUSE`, `FINANCE`) are permitted.
* **Tenant Isolation**: Every database query and tool invocation strictly filters by `tenantId`. Attempting to access data from another tenant returns an immediate policy violation.

---

## 2. Deterministic Attention Signal Engine

To answer questions like *"Which bookings need attention tomorrow?"*, Day 28 introduces `BookingAttentionService`. Rather than delegating reasoning to an LLM, the engine inspects live entities using 12 deterministic signals:

| Attention Signal | Domain Source | Trigger Condition | Severity |
| :--- | :--- | :--- | :--- |
| `INVENTORY_CONFLICT` | `AvailabilityService` | Over-allocated product without adequate buffer | `HIGH` |
| `WAREHOUSE_NOT_READY` | `WarehouseService` | Scheduled start within 24h but order is not packed | `HIGH` |
| `PICKING_SHORTAGE` | `WarehouseException` | Open pick shortage exception logged | `HIGH` |
| `PACKING_INCOMPLETE` | `WarehouseOrderItem` | Packed quantity is less than required | `MEDIUM` |
| `UNSIGNED_CONTRACT` | `Booking` | Rental start is imminent and contract is unsigned | `MEDIUM` |
| `UNPAID_DEPOSIT` | `Booking` / `Payment` | Security deposit required > deposit paid | `HIGH` |
| `OVERDUE_INVOICE` | `Invoice` | Customer has past-due balance on prior invoices | `MEDIUM` |
| `DELIVERY_UNASSIGNED` | `Delivery` | Scheduled delivery within 24h lacks an assigned driver | `HIGH` |
| `VEHICLE_UNASSIGNED` | `Delivery` | Scheduled delivery within 24h lacks a fleet vehicle | `HIGH` |
| `RETURN_OVERDUE` | `ReturnService` | Return date has passed and items are not checked in | `MEDIUM` |
| `CUSTOMER_CHANGE_REQUEST` | `CRM Activity` | Unacknowledged date or item modification request | `MEDIUM` |
| `OPEN_DAMAGE_ISSUE` | `DamageClaim` | Pending damage claim on prior booking for same equipment | `LOW` |

---

## 3. Controlled Business Tools

Registered in `AiSalesToolRegistry` under package `com.rentflow.aisales.tool`:

1. **`GetExecutiveDashboardTool`**: Fetches MTD booked revenue, collected revenue, outstanding revenue, gross margin %, and order count from `AnalyticsService`.
2. **`GetProfitabilitySummaryTool`**: Evaluates booking-level profitability and flags low/negative margin bookings.
3. **`GetMarginTrendTool`**: Computes margin delta between periods and breaks down variance across delivery vehicle costs, setup labor overtime, and unrecovered damage claims.
4. **`GetArAgingTool`**: Buckets receivables into 0–30, 31–60, 61–90, and 90+ days overdue; identifies top debtor accounts.
5. **`GetBookingsNeedingAttentionTool`**: Evaluates upcoming bookings against the 12 attention signals.
6. **`GetWarehouseBlockersTool`**: Analyzes open fulfillment exceptions and unassigned pick orders.
7. **`GetDeliveryRisksTool`**: Identifies dispatches missing drivers, vehicles, or packed equipment.
8. **`GetSalesFollowUpsDueTool`**: Surfaces overdue CRM tasks and tasks due today.
9. **`GetQuotesAwaitingResponseTool`**: Lists pending quotes sent to customers awaiting approval.
10. **`SearchCustomersTool`**: Provides fuzzy search with multi-match candidate disambiguation.
11. **`GetIntegrationHealthTool`**: Verifies external sync jobs and event outbox health.
12. **`ProposeCopilotActionTool`**: Drafts an action proposal for human confirmation.

---

## 4. Human-in-the-Loop Action Proposal Flow

```
User Query: "Assign Alex Driver to DEL-000123"
   │
   ▼
[CopilotActionService.proposeAction]
   │ Creates CopilotActionProposal (Status: PROPOSED, Risk: MEDIUM, Expiry: 30m)
   ▼
UI Action Card Rendered:
   ┌─────────────────────────────────────────────────────────────┐
   │ [ASSIGN_DRIVER]  Risk: MEDIUM  Status: PROPOSED             │
   │ Assign driver Alex Driver to delivery DEL-000123            │
   │                                                             │
   │     [✓ Confirm & Execute]          [✕ Cancel]               │
   └─────────────────────────────────────────────────────────────┘
          │                                 │
   Clicks Confirm                    Clicks Cancel
          │                                 │
          ▼                                 ▼
POST /api/copilot/actions/{id}/confirm   POST /api/copilot/actions/{id}/cancel
• Re-verifies tenant isolation           • Sets status = CANCELLED
• Checks user role (OPERATIONS/OWNER)   • No state changes made
• Executes DeliveryService.assignDriver
• Logs audit record & notifies driver
• Sets status = EXECUTED
```

---

## 5. Security Model & Defensive Guardrails

* **Prompt Injection Defense**: Guardrail filters detect adversarial instructions ("ignore all rules", "reveal your system prompt", "show API key"). The Copilot rejects the request and reminds the user of platform security policies.
* **Absence of Data Defense**: When asked for dates or entities outside platform records (e.g. *"What was our profit in 2018?"*), the Copilot explicitly clarifies that records start in 2024 and refuses to invent fictional metrics.
* **Disambiguation on Multiple Matches**: When searching for entities with ambiguous names (e.g. *"Find customer Smith"*), Copilot returns all matching candidates and prompts the user to select the appropriate record.
* **Contextual Page Inquiries**: When navigating RentFlow pages (such as `/bookings/{id}`), the UI passes `pageContextType=BOOKING` and `pageContextId={id}`. Asking *"Why is this blocked?"* automatically evaluates the specific entity's blockers.

---

## 6. Verification & Test Coverage

Three dedicated Spring Boot test suites validate the Day 28 Copilot capabilities:

1. **`Day28CopilotAnalyticsTest`**:
   * Validates executive dashboard KPIs, gross margin calculation, and role authorization.
   * Tests explainable margin trend decomposition and data quality notices.
   * Tests accounts receivable aging buckets (0–30, 31–60, 61–90, 90+ days).
   * Verifies historical data boundary enforcement without hallucination.

2. **`Day28CopilotOperationalTest`**:
   * Validates deterministic attention signal generation across real bookings.
   * Tests warehouse blocker detection and unassigned pick order identification.
   * Tests delivery risk identification for unassigned drivers and vehicles.
   * Tests sales follow-up queries and quote expiration tracking.
   * Tests customer search with multi-match candidate disambiguation.
   * Tests page contextual inquiry (`Why is this blocked?`).

3. **`Day28CopilotActionAndSecurityTest`**:
   * Validates that the `CUSTOMER` role is strictly forbidden with `403/SecurityException`.
   * Tests prompt injection and secret exfiltration defense.
   * Tests prohibited action rejection (refunds, cancellations, payment capture).
   * Tests cross-tenant isolation enforcement.
   * Tests full action proposal lifecycle: creation, human confirmation, execution, and cancellation.
   * Tests that unauthorized roles cannot execute sensitive domain actions.
