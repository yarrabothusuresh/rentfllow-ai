# RentFlow AI — Day 12 Architecture & Release Documentation

## Module: Invoice Management

### 1. Objective
Implement the complete **Invoice Management** module for RentFlow AI. The system allows rental companies to generate itemized invoices from confirmed or active bookings, view invoice details and snapshot line items, track invoice payment status and outstanding balances, link payments to invoices, mark invoices as sent, void invoices with strict RBAC authorization, audit invoice lifecycle events, and render professional US rental SaaS invoice screens without live payment gateway or SMTP integrations today.

---

### 2. Existing Components Reused
- **Entities Reused**: `Booking`, `BookingItem`, `Customer`, `Product`, `Payment`.
- **Payment Integration**: Reused `PaymentService` from Day 11 to track payments, calculate `amountPaid` and `balanceDue`, and update invoice statuses (`PARTIALLY_PAID`, `PAID`, `OVERDUE`).
- **Tenant Isolation**: Multi-tenant isolation enforced via `tenantId` (`X-Tenant-Id` header).
- **RBAC**: Enforced via security header `X-User-Role` (`OWNER`, `ADMIN`, `FINANCE` have full write/void access, `SALES` can view/create, `WAREHOUSE` read-only, `CUSTOMER` view own).
- **Audit System**: Audit pattern aligned with `PaymentAudit` and `InventoryTransaction` models (`INVOICE_CREATED`, `INVOICE_SENT`, `INVOICE_STATUS_CHANGED`, `INVOICE_VOIDED`).
- **Monetary Representation**: Uniform `BigDecimal` precision (10, 2) across all entities and calculations using `RoundingMode.HALF_UP`.
- **IDs**: UUID generation for primary keys.

---

### 3. Database Schema

#### `invoices` Table
| Column | Type | Constraints | Description |
|---|---|---|---|
| `id` | UUID | PRIMARY KEY | Unique invoice identifier |
| `tenant_id` | VARCHAR | NOT NULL, INDEXED | Multi-tenant isolation ID |
| `booking_id` | UUID | NOT NULL, INDEXED | Associated booking ID |
| `customer_id` | UUID | NOT NULL, INDEXED | Associated customer ID |
| `invoice_number` | VARCHAR | NOT NULL, UNIQUE (per tenant) | Format: `INV-000001` |
| `issue_date` | DATE | NOT NULL | Invoice issuance date |
| `due_date` | DATE | NOT NULL | Invoice payment due date |
| `subtotal` | NUMERIC(10,2) | NOT NULL | Sum of line items subtotal |
| `discount` | NUMERIC(10,2) | NOT NULL | Applied discount amount |
| `fees` | NUMERIC(10,2) | NOT NULL | Sum of delivery/pickup/setup fees |
| `tax` | NUMERIC(10,2) | NOT NULL | Calculated sales tax |
| `total_amount` | NUMERIC(10,2) | NOT NULL | Subtotal - Discount + Fees + Tax |
| `amount_paid` | NUMERIC(10,2) | NOT NULL | Total completed payments received |
| `balance_due` | NUMERIC(10,2) | NOT NULL | Total Amount - Amount Paid |
| `status` | VARCHAR | NOT NULL, INDEXED | Enum: `DRAFT`, `SENT`, `PARTIALLY_PAID`, `PAID`, `OVERDUE`, `VOID` |
| `notes` | VARCHAR(2000) | NULLABLE | Customer notes |
| `customer_name` | VARCHAR | NOT NULL | Billing snapshot: Customer full name |
| `company_name` | VARCHAR | NULLABLE | Billing snapshot: Customer company name |
| `email` | VARCHAR | NULLABLE | Billing snapshot: Customer email |
| `phone` | VARCHAR | NULLABLE | Billing snapshot: Customer phone |
| `billing_address` | VARCHAR | NULLABLE | Billing snapshot: Street address |
| `city` | VARCHAR | NULLABLE | Billing snapshot: City |
| `state` | VARCHAR | NULLABLE | Billing snapshot: State |
| `zip_code` | VARCHAR | NULLABLE | Billing snapshot: Postal code |
| `country` | VARCHAR | NULLABLE | Billing snapshot: Country |
| `created_by` | VARCHAR | NULLABLE | User role / ID who created invoice |
| `created_at` | TIMESTAMP | NOT NULL | Creation timestamp |
| `updated_at` | TIMESTAMP | NOT NULL | Last update timestamp |

#### `invoice_items` Table
| Column | Type | Constraints | Description |
|---|---|---|---|
| `id` | UUID | PRIMARY KEY | Unique item ID |
| `invoice_id` | UUID | NOT NULL, INDEXED | Parent invoice ID |
| `product_id` | UUID | NULLABLE, INDEXED | Reference product ID |
| `description` | VARCHAR | NOT NULL | Line item description |
| `quantity` | INT | NOT NULL | Item quantity (> 0) |
| `unit_price` | NUMERIC(10,2) | NOT NULL | Snapshot unit price |
| `discount` | NUMERIC(10,2) | NOT NULL | Line discount |
| `tax` | NUMERIC(10,2) | NOT NULL | Line tax amount |
| `line_total` | NUMERIC(10,2) | NOT NULL | Snapshot quantity * unit_price |
| `created_at` | TIMESTAMP | NOT NULL | Creation timestamp |

#### `invoice_audits` Table
| Column | Type | Constraints | Description |
|---|---|---|---|
| `id` | UUID | PRIMARY KEY | Unique audit ID |
| `tenant_id` | VARCHAR | NOT NULL, INDEXED | Multi-tenant isolation ID |
| `booking_id` | UUID | NULLABLE, INDEXED | Associated booking ID |
| `invoice_id` | UUID | NULLABLE, INDEXED | Target invoice ID |
| `action` | VARCHAR | NOT NULL | Event name (`INVOICE_CREATED`, `INVOICE_SENT`, `INVOICE_STATUS_CHANGED`, `INVOICE_VOIDED`) |
| `performed_by` | VARCHAR | NULLABLE | User role performing action |
| `details` | VARCHAR(2000) | NULLABLE | Human readable event details |
| `timestamp` | TIMESTAMP | NOT NULL | Event timestamp |

---

### 4. REST API Endpoints Specification

| Method | Path | Request Body | Description | Allowed Roles |
|---|---|---|---|---|
| `GET` | `/api/invoices` | Query params (status, customerId, bookingId, search, page, size) | List invoices with filtering, pagination, and sorting | `OWNER`, `ADMIN`, `FINANCE`, `SALES`, `WAREHOUSE`, `CUSTOMER` |
| `GET` | `/api/invoices/{invoiceId}` | None | Retrieve detailed invoice with items, billing snapshot, and payments | `OWNER`, `ADMIN`, `FINANCE`, `SALES`, `CUSTOMER` |
| `POST` | `/api/invoices/from-booking/{bookingId}` | Optional notes/due date | Generate invoice from booking (snapshots prices/items) | `OWNER`, `ADMIN`, `FINANCE`, `SALES` |
| `PATCH` | `/api/invoices/{invoiceId}/status` | `{ "status": "SENT" }` | Transition invoice status (e.g. DRAFT -> SENT) | `OWNER`, `ADMIN`, `FINANCE` |
| `POST` | `/api/invoices/{invoiceId}/void` | `{ "reason": "..." }` | Void an invoice (prevents voiding if active payments exist) | `OWNER`, `ADMIN`, `FINANCE` |
| `GET` | `/api/bookings/{bookingId}/invoice` | None | Retrieve invoice associated with booking | `OWNER`, `ADMIN`, `FINANCE`, `SALES`, `CUSTOMER` |
| `GET` | `/api/invoices/{invoiceId}/payments` | None | Retrieve payments linked to invoice's booking | `OWNER`, `ADMIN`, `FINANCE`, `SALES`, `CUSTOMER` |

---

### 5. Invoice Lifecycle & State Transitions

```
                    [ DRAFT ]
                       │
                       ▼
                    [ SENT ]
                       │
          ┌────────────┴────────────┐
          ▼                         ▼
   [ PARTIALLY_PAID ]           [ OVERDUE ]
          │                         │
          └────────────┬────────────┘
                       ▼
                    [ PAID ]

 (Any active status except PAID can transition to VOID with authorization)
```

#### Transition Rules:
1. `DRAFT` -> `SENT`: Allowed upon marking as sent.
2. `SENT` -> `PARTIALLY_PAID`: Triggered when completed payments > 0 but balance > 0.
3. `SENT` / `PARTIALLY_PAID` -> `PAID`: Triggered when completed payments equal total amount (balance due = $0).
4. `SENT` / `PARTIALLY_PAID` -> `OVERDUE`: Derived/updated if `currentDate > dueDate` and `balanceDue > 0`.
5. `PAID` -> `DRAFT` or `SENT`: **Forbidden**. Paid invoices cannot revert to draft or sent.
6. `VOID` -> Any status: **Forbidden**. Voided invoices are immutable.

---

### 6. Financial Snapshot Architecture & Price Protection
- Invoices store a **complete snapshot** of item descriptions, quantities, unit prices, subtotal, discounts, fees, and tax at the moment of invoice generation.
- If catalog product prices increase in the future, existing issued invoices remain unchanged.
- If customer addresses or company names change in CRM, the invoice's billing snapshot preserves historical accuracy.

---

### 7. Tax Architecture (`TaxService`)
- Created a pluggable `TaxService` interface with `DemoTaxServiceImpl` supporting configurable state tax rates (default `8.25%`).
- Design decouples tax logic from controllers and entities, preparing for future integration with US tax calculation engines (e.g. Avalara, TaxJar).

---

### 8. PDF Preparation (`InvoiceDocumentService`)
- Implemented `InvoiceDocumentService` abstraction providing `generateInvoicePdf(invoiceId)`. Returns stubbed PDF metadata/bytes for future PDF rendering engine integration.

---

### 9. AI Copilot Integration (`GetInvoiceSummaryTool`)
- Created read-only AI tool [`GetInvoiceSummaryTool.java`](file:///c:/dev/rentflow-ai/backend/src/main/java/com/rentflow/ai/tool/GetInvoiceSummaryTool.java) enabling Day 26 AI Copilot to answer queries such as:
  - *"Show me all overdue invoices."*
  - *"Which customers owe more than $2,000?"*
- Tool prohibits status changes, voiding, or payment mutations.

---

### 10. Demo Data Initializer
`InvoiceDataInitializer` seeds realistic demo invoices:
1. **INV-000001**: Emily's Wedding / ABC Events LLC. Total $2,500.00, Paid $500.00, Balance $2,000.00, Status `PARTIALLY_PAID`.
2. **INV-000002**: TechCorp Annual Gala. Total $5,000.00, Paid $5,000.00, Balance $0.00, Status `PAID`.
3. **INV-000003**: Corporate Leadership Retreat (Overdue). Total $1,200.00, Paid $0.00, Balance $1,200.00, Due Date 07/15/2026, Status `OVERDUE`.

---

### 11. Known Limitations & Future Roadmap
- **No Live Stripe Integration Today**: Payment recording links with Day 11 manual payment system; future release will link Stripe PaymentIntents.
- **No Live Email Dispatch Today**: Marking as sent records audit log; future release will integrate SMTP/SendGrid templates.
