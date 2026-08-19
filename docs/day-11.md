# RentFlow AI — Day 11 Architecture & Release Documentation

## Module: Payments & Deposits

### 1. Objective
Implement the complete **Payments & Deposits** tracking module for RentFlow AI without integrating live third-party payment gateways (e.g. Stripe, PayPal) today. The system enables recording deposits, partial payments, full payments, balance recalculation, voiding payments, payment audit logging, multi-tenant isolation, RBAC security, and an updated Angular Booking Details UX.

---

### 2. Existing Components Reused
- **Entities Reused**: `Booking`, `Customer`, `Event`, `Product`, `RoleType`.
- **Tenant Isolation**: Multi-tenant protection preserved using `tenantId` (`X-Tenant-Id` header).
- **RBAC**: Security header `X-User-Role` enforced across backend services and controllers.
- **Audit System**: Audit pattern aligned with `InventoryTransaction` / `Event` models; audit records track `PAYMENT_RECORDED` and `PAYMENT_VOIDED`.
- **Monetary Representation**: Uniform `BigDecimal` precision (10, 2) across all backend entities and DTOs.
- **IDs**: UUID generation for entity primary keys.
- **Angular Integration**: Upgraded [`booking-detail.component.html`](file:///c:/dev/rentflow-ai/frontend/src/app/pages/bookings/booking-detail.component.html) to render Financial Summary, Payment History table, and Record/Void Payment modals.

---

### 3. Database Changes

#### `payment` Table
| Column | Type | Constraints | Description |
|---|---|---|---|
| `id` | UUID | PRIMARY KEY | Unique payment identifier |
| `tenant_id` | VARCHAR | NOT NULL, INDEXED | Multi-tenant isolation ID |
| `booking_id` | UUID | NOT NULL, INDEXED | Associated booking ID |
| `customer_id` | UUID | NOT NULL, INDEXED | Associated customer ID |
| `amount` | NUMERIC(10,2) | NOT NULL | Payment amount |
| `payment_method` | VARCHAR | NOT NULL | Enum: `CASH`, `BANK_TRANSFER`, `CREDIT_CARD`, `DEBIT_CARD`, `CHECK`, `OTHER` |
| `payment_status` | VARCHAR | NOT NULL, INDEXED | Enum: `PENDING`, `COMPLETED`, `FAILED`, `REFUNDED`, `VOID` |
| `payment_date` | DATE | NOT NULL | Transaction date |
| `transaction_reference` | VARCHAR | NULLABLE | Transaction / check / bank reference |
| `notes` | VARCHAR(2000) | NULLABLE | Payment notes |
| `created_by` | VARCHAR | NULLABLE | User role / ID who recorded the payment |
| `created_at` | TIMESTAMP | NOT NULL | Creation timestamp |
| `updated_at` | TIMESTAMP | NOT NULL | Last update timestamp |

#### `payment_audit` Table
| Column | Type | Constraints | Description |
|---|---|---|---|
| `id` | UUID | PRIMARY KEY | Unique audit log ID |
| `tenant_id` | VARCHAR | NOT NULL, INDEXED | Multi-tenant isolation ID |
| `booking_id` | UUID | NOT NULL, INDEXED | Associated booking ID |
| `payment_id` | UUID | NULLABLE, INDEXED | Target payment ID |
| `action` | VARCHAR | NOT NULL | Event name (`PAYMENT_RECORDED`, `PAYMENT_VOIDED`) |
| `performed_by` | VARCHAR | NULLABLE | User role performing action |
| `details` | VARCHAR(2000) | NULLABLE | Human-readable audit text |
| `timestamp` | TIMESTAMP | NOT NULL | Action timestamp |

---

### 4. REST API Endpoint Specification

| Method | Path | Request Body | Description | Allowed Roles |
|---|---|---|---|---|
| `GET` | `/api/bookings/{bookingId}/payments` | None | Retrieves all payment records for a booking | `OWNER`, `ADMIN`, `FINANCE`, `SALES`, `WAREHOUSE`, `CUSTOMER` |
| `POST` | `/api/bookings/{bookingId}/payments` | `RecordPaymentDTO` | Records a new manual deposit / payment | `OWNER`, `ADMIN`, `FINANCE` |
| `GET` | `/api/payments/{paymentId}` | None | Retrieves single payment details | `OWNER`, `ADMIN`, `FINANCE`, `SALES` |
| `POST` | `/api/payments/{paymentId}/void` | `{ "reason": "..." }` | Voids a payment & recalculates balance | `OWNER`, `ADMIN`, `FINANCE` |
| `GET` | `/api/bookings/{bookingId}/financial-summary` | None | Retrieves booking financial summary | `OWNER`, `ADMIN`, `FINANCE`, `SALES`, `WAREHOUSE`, `CUSTOMER` |

---

### 5. Financial Calculations & State Transitions

#### Formulas
- **Booking Total**: Sum of booking items subtotal, fees, tax minus discounts.
- **Deposit Paid (`amountPaid`)**: Sum of all `COMPLETED` payments for the booking.
- **Outstanding Balance**: `max(0, Total Booking Amount - Deposit Paid)`.

#### Financial Status State Transitions
```
Booking Created ($2,500 total)
       │
       ▼
   $0 Paid ──► [ DEPOSIT_PENDING ]
       │
   $500 Paid ──► [ PARTIALLY_PAID ] (Outstanding: $2,000)
       │
 $2,500 Paid ──► [ PAID ] (Outstanding: $0)
       │
 Void Payment ──► Recalculate Sum ──► Return to [ PARTIALLY_PAID ] or [ DEPOSIT_PENDING ]
```

---

### 6. Validation Rules & Transaction Safety
1. **Positive Amount**: Payment amount must be strictly greater than zero (`amount > 0`).
2. **Tenant Isolation**: Payments cannot be recorded or fetched across different tenant IDs.
3. **Active Booking Validation**: Payments are blocked against cancelled bookings.
4. **Overpayment Prevention**: Single or cumulative payments exceeding the outstanding balance are rejected with explicit message:
   `"Payment exceeds outstanding balance of $X."`
5. **Void Integrity**: Payments already in status `VOID` cannot be voided again.
6. **Atomicity (`@Transactional`)**: Payment insertion, booking balance/status update, and audit logging execute within a single atomic database transaction. Any failure triggers a complete rollback.

---

### 7. RBAC Matrix

| Role | View Financial Summary | View Payments | Record Payment | Void Payment |
|---|:---:|:---:|:---:|:---:|
| `OWNER` | ✅ | ✅ | ✅ | ✅ |
| `ADMIN` | ✅ | ✅ | ✅ | ✅ |
| `FINANCE` | ✅ | ✅ | ✅ | ✅ |
| `SALES` | ✅ | ✅ | ❌ | ❌ |
| `WAREHOUSE` | ✅ (Status only) | ❌ | ❌ | ❌ |
| `DRIVER` | ❌ | ❌ | ❌ | ❌ |
| `CUSTOMER` | ✅ (Own booking) | ✅ (Own booking) | ❌ | ❌ |

---

### 8. AI Preparation (`GetBookingPaymentSummaryTool`)
Created a read-only tool abstraction [`GetBookingPaymentSummaryTool.java`](file:///c:/dev/rentflow-ai/backend/src/main/java/com/rentflow/ai/tool/GetBookingPaymentSummaryTool.java) allowing Day 26 AI Copilot to answer queries like:
*"How much does ABC Events still owe?"*
The tool returns structured totals without permitting payment mutations or status alterations.

---

### 9. Demo Data Initializer
`PaymentDataInitializer` seeds:
1. **Emily's Wedding / ABC Events LLC**: Booking Total $2,500.00, Deposit Required $750.00, Paid $500.00 (`BANK_TRANSFER`, `BANK-12345`), Outstanding Balance $2,000.00, Status `PARTIALLY_PAID`.
2. **TechCorp Annual Gala**: Booking Total $1,500.00, Paid $1,500.00 (`CREDIT_CARD`, `CC-98765`), Outstanding Balance $0.00, Status `PAID`.

---

### 10. Known Limitations & Future Gateway Integration Strategy
- **Manual Gateway Only**: Day 11 implements manual recording (`BANK_TRANSFER`, `CREDIT_CARD`, `CHECK`, etc.).
- **Future Stripe/Gateway Architecture**: The service design easily integrates Stripe Webhooks or PaymentIntents by updating `PaymentStatus` upon receiving webhook events without changing domain entities or financial status calculation contracts.
