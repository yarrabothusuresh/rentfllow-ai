# Day 26 — CRM, Leads & Inquiry Management

## 1. Objective & Scope
RentFlow AI Day 26 implements a dedicated rental-industry CRM pipeline connecting customer inquiries, storefront rental requests, manual sales leads, customers, events, and quotes into one unified sales workflow:
```
CUSTOMER INQUIRY / STOREFRONT REQUEST / PHONE / EMAIL / WALK-IN
                         ↓
                       LEAD (NEW)
                         ↓
               ASSIGN SALES REPRESENTATIVE
                         ↓
                      CONTACTED
                         ↓
                  NEEDS DISCOVERY
                         ↓
                     QUALIFIED
                         ↓
             LINK / CREATE CUSTOMER & EVENT
                         ↓
                  QUOTE PREPARED (DRAFT)
                         ↓
                    QUOTE SENT
                         ↓
              FOLLOW-UP / NEGOTIATION
                         ↓
                  WON (BOOKING CONFIRMED) / LOST
```

---

## 2. Existing Architecture Findings & Reusability Analysis

| Domain / Concept | Existing Codebase State | Day 26 Reuse & Extension Strategy |
| :--- | :--- | :--- |
| **Lead Entity** | Basic legacy `Lead` entity in `com.rentflow.ai.model.Lead` with primitive fields (`status`, `firstName`, `email`). | **Extend existing aggregate**: Upgrade `Lead` with tenant-scoped sequential numbering (`LEAD-000001`), `LeadSource`, `LeadStage`, `LeadPriority`, `LeadLostReason`, financial budget/value fields, customer ID, rental request ID, quote ID, booking ID, follow-up timestamps, and lifecycle timestamps. |
| **Customer Management** | `CustomerService`, `CustomerRepository`, `Customer` model with unique customer numbering and email checks. | **Reuse strictly**: `POST /api/crm/leads/{id}/convert-customer` calls `CustomerService` and checks for duplicate/matching email/phone/company, providing a choice to link existing or create new. |
| **Quote Workflow** | `QuoteService`, `QuoteCalculationService`, `Quote` entity with item lines, tax, delivery, and pricing engines. | **Reuse strictly**: Quote generation from a Lead invokes `QuoteService`. Quote status transitions (`SENT`, `ACCEPTED`) publish domain events synchronized back to the Lead CRM stage. |
| **Storefront & Requests** | `CustomerRequest`, `RentalCart`, `CartService`, `StorefrontController`. | **Idempotent bridge**: When a storefront rental request is created, publish `CustomerRequestCreatedEvent` handled by `CrmEventListener` to automatically create a linked `Lead` (`STOREFRONT_REQUEST`) with database-level idempotency on `tenantId + rentalRequestId`. |
| **Notifications** | `NotificationService` supporting internal user alerts with priority and reference links. | **Reuse**: Trigger notifications on `NEW_RENTAL_INQUIRY`, `LEAD_ASSIGNED`, `FOLLOW_UP_DUE`, `FOLLOW_UP_OVERDUE`, and `HIGH_PRIORITY_LEAD`. |
| **Auditing & History** | `AuditService` recording security and state transitions. | **Reuse**: Audit all stage transitions, assignment changes, lost closures, and reopenings. |
| **Tenant Isolation & Security** | `SecurityUtils.getCurrentTenantId()`, `SecurityUtils.getCurrentUser()`, `PermissionCode`. | **Strict Tenant Scoping**: All queries filter by `tenantId`. Added permissions: `CRM_VIEW`, `CRM_MANAGE`, `CRM_ASSIGN`, `CRM_QUALIFY`, `CRM_QUOTE`. Forbidden for `CUSTOMER` and `DRIVER` roles. |

---

## 3. CRM Domain Aggregate & Data Model

### A. Core Aggregate: `Lead`
- **Identity & Multi-tenancy**: `id` (UUID), `tenantId` (String), `leadNumber` (`LEAD-000001`, globally unique within tenant).
- **Contact Details**: `firstName`, `lastName`, `contactName`, `companyName`, `email`, `phone`, `preferredContactMethod` (`EMAIL`, `PHONE`, `SMS`).
- **Opportunity / Event Context**: `eventName`, `eventType`, `eventDate`, `rentalStartDate`, `rentalEndDate`, `venueName`, `venueAddress`, `guestCount`, `estimatedBudget`, `estimatedValue`, `customerNotes`, `internalNotes`.
- **Classification**:
  - `source`: `STOREFRONT_REQUEST`, `WEBSITE_INQUIRY`, `PHONE`, `EMAIL`, `WALK_IN`, `REFERRAL`, `SOCIAL`, `MANUAL`, `OTHER`.
  - `stage`: `NEW`, `CONTACTED`, `NEEDS_DISCOVERY`, `QUALIFIED`, `QUOTE_PREPARED`, `QUOTE_SENT`, `FOLLOW_UP`, `NEGOTIATION`, `WON`, `LOST`, `DISQUALIFIED`.
  - `priority`: `LOW`, `NORMAL`, `HIGH`, `URGENT`.
  - `lostReason`: `PRICE`, `AVAILABILITY`, `DATE_CHANGED`, `COMPETITOR`, `NO_RESPONSE`, `EVENT_CANCELLED`, `NOT_A_FIT`, `DUPLICATE`, `CUSTOMER_CANCELLED`, `OTHER`.
  - `lostReasonNotes`: Additional context for post-mortem sales analysis.
- **Assignment & Foreign Linkages**:
  - `assignedSalesUserId`, `assignedSalesUserName`.
  - `rentalRequestId` (Unique per tenant when present).
  - `customerId`, `customerName`.
  - `quoteId`, `quoteNumber`.
  - `bookingId`, `bookingNumber`.
- **Milestone Timestamps**: `lastContactedAt`, `nextFollowUpAt`, `qualifiedAt`, `convertedAt`, `wonAt`, `lostAt`, `reopenedAt`, `createdAt`, `updatedAt`, `createdBy`, `updatedBy`.

### B. Supporting Aggregate: `LeadActivity`
Chronological activity timeline tracking every sales interaction and system event:
- `id`, `tenantId`, `leadId`.
- `type`: `NOTE`, `CALL`, `EMAIL`, `SMS`, `MEETING`, `STATUS_CHANGE`, `ASSIGNMENT`, `FOLLOW_UP_CREATED`, `FOLLOW_UP_COMPLETED`, `RENTAL_REQUEST_LINKED`, `CUSTOMER_LINKED`, `CUSTOMER_CREATED`, `QUOTE_CREATED`, `QUOTE_SENT`, `WON`, `LOST`, `REOPENED`.
- `direction`: `INBOUND`, `OUTBOUND`, `INTERNAL`.
- `subject`, `summary`, `notes`, `callOutcome` (`CONNECTED`, `NO_ANSWER`, `VOICEMAIL`, `FOLLOW_UP_REQUIRED`), `occurredAt`, `createdBy`.

### C. Supporting Aggregate: `LeadFollowUp`
Actionable task management for sales representatives:
- `id`, `tenantId`, `leadId`, `assignedTo`.
- `type`: `CALL`, `EMAIL`, `SMS`, `MEETING`, `QUOTE`, `GENERAL`, `OTHER`.
- `title`, `notes`, `dueAt`.
- `status`: `OPEN`, `COMPLETED`, `CANCELLED`, `OVERDUE` (dynamically derived when `dueAt < now` and status is `OPEN`).
- `completedAt`, `completedBy`.

---

## 4. Sales Pipeline Stage Transitions & Validation Rules

The backend strictly validates allowed stage transitions:
```
NEW ──→ CONTACTED ──→ NEEDS_DISCOVERY ──→ QUALIFIED ──→ QUOTE_PREPARED ──→ QUOTE_SENT ──→ FOLLOW_UP / NEGOTIATION ──→ WON
 │           │               │                 │               │               │                  │
 └───────────┴───────────────┴─────────────────┴───────────────┴───────────────┴──────────────────┴──────→ LOST / DISQUALIFIED
```
1. **Qualification**: Setting stage to `QUALIFIED` records `qualifiedAt` and requires basic contact & event intention.
2. **Disqualification**: Moving to `DISQUALIFIED` requires an explicit reason (e.g. out of service area, spam, non-rental).
3. **Lost**: Transition to `LOST` requires `lostReason` and optional `lostReasonNotes`, stamping `lostAt`.
4. **Reopening**: An authorized manager or salesperson can reopen a `LOST` lead, recording a mandatory reason in the activity timeline without erasing prior history.
5. **Quote Synchronization**: Creating a quote advances the lead to `QUOTE_PREPARED`; sending the quote advances it to `QUOTE_SENT`. Confirming the booking marks the lead `WON`.

---

## 5. Public Storefront & Website Inquiry Integration

- **Public Endpoint**: `POST /api/public/storefront/{tenantSlug}/inquiries`
- **Tenant Resolution**: Resolves tenant securely from `tenantSlug` (never trusting client-supplied tenant ID).
- **Sanitization & Anti-Spam**: Validates email format, sanitizes inputs against XSS, and rejects oversized payloads.
- **Lead Generation**: Automatically initializes a `Lead` in `NEW` stage with `WEBSITE_INQUIRY` source.
- **Internal Alert**: Dispatches `NEW_RENTAL_INQUIRY` in-app notification to the sales team.

---

## 6. REST API Design

| Method | Endpoint | Description |
|---|---|---|
| `GET` | `/api/crm/dashboard` | Returns operational KPI counts (New, Unassigned, Qualified, Due Today, Overdue, Won, Lost, etc.). |
| `GET` | `/api/crm/pipeline` | Returns leads grouped by stage with aggregated pipeline values. |
| `GET` | `/api/crm/leads` | Filterable, paginated lead list with multi-field search and quick tabs. |
| `POST` | `/api/crm/leads` | Manually capture a new sales lead. |
| `GET` | `/api/crm/leads/{id}` | Detailed lead record with linked customer, request, quote, and booking. |
| `PATCH` | `/api/crm/leads/{id}` | Update lead fields, budget, notes, dates. |
| `POST` | `/api/crm/leads/{id}/assign` | Assign, reassign, or unassign sales representative. |
| `POST` | `/api/crm/leads/{id}/transition` | Validated stage progression. |
| `POST` | `/api/crm/leads/{id}/qualify` | Mark lead as qualified. |
| `POST` | `/api/crm/leads/{id}/disqualify` | Mark lead as disqualified with reason. |
| `POST` | `/api/crm/leads/{id}/won` | Mark lead won with linked quote/booking. |
| `POST` | `/api/crm/leads/{id}/lost` | Mark lead lost with required `LeadLostReason`. |
| `POST` | `/api/crm/leads/{id}/reopen` | Reopen a lost/disqualified lead with reason. |
| `GET` | `/api/crm/leads/{id}/activities` | Retrieve chronological activity timeline. |
| `POST` | `/api/crm/leads/{id}/activities` | Log call, email, note, or meeting. |
| `GET` | `/api/crm/leads/{id}/follow-ups` | Retrieve scheduled follow-up tasks. |
| `POST` | `/api/crm/leads/{id}/follow-ups` | Schedule new follow-up task. |
| `POST` | `/api/crm/follow-ups/{id}/complete` | Mark follow-up completed. |
| `POST` | `/api/crm/follow-ups/{id}/cancel` | Cancel follow-up. |
| `POST` | `/api/crm/leads/{id}/link-customer` | Link lead to an existing customer ID. |
| `POST` | `/api/crm/leads/{id}/convert-customer` | Convert lead into a new Customer via `CustomerService`. |
| `POST` | `/api/crm/leads/{id}/quote` | Launch quote draft creation pre-populated from lead. |
| `POST` | `/api/public/storefront/{tenantSlug}/inquiries` | Public website inquiry submission. |

---

## 7. Angular User Experience

1. **CRM Operations Dashboard** (`/crm/dashboard`):
   - Metric cards: New Leads, Unassigned Leads, My Active Leads, Qualified Leads, Follow-Ups Due Today, Overdue Follow-Ups, Quotes Prepared, Quotes Sent, Won, Lost.
   - Quick action shortcuts: Capture Lead, View Pipeline, Unassigned Queue.
2. **Leads Management Screen** (`/crm/leads`):
   - Filter bar: Search, Stage, Source, Priority, Assigned User, "My Leads" filter, "Unassigned" filter.
   - High-density table with responsive cards for mobile.
3. **Lead Detail Console** (`/crm/leads/:id`):
   - Left Pane: Opportunity overview, contact info, rental dates, budget, estimated value, linked customer/quote badges.
   - Stage progression tracker with contextual transition buttons.
   - Action toolbar: Log Call, Log Email, Add Note, Schedule Follow-Up, Qualify, Convert Customer, Create Quote, Mark Won, Mark Lost.
   - Right Pane: Chronological activity timeline and scheduled tasks.
4. **Interactive Pipeline Kanban** (`/crm/pipeline`):
   - Drag-and-drop / action-based column cards across `NEW`, `CONTACTED`, `DISCOVERY`, `QUALIFIED`, `QUOTE`, `FOLLOW_UP`, `NEGOTIATION`, `WON`, `LOST`.
   - Stage summary headers showing total opportunities and aggregated pipeline value ($).
5. **Public Contact Form** (`/store/:tenantSlug/contact`):
   - Clean, customer-safe inquiry form with instant submission feedback and rate-limiting protection.
