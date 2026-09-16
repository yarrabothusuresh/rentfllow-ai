# RentFlow AI — Day 34: Payment Idempotency & Financial Transaction Integrity

## 1. Overview & Core Financial Invariant
Financial correctness is a P0 requirement in RentFlow AI. The overarching invariant of the payment system is:
> **A single financial payment event must affect RentFlow SaaS balances and records exactly once.**

Duplicate submissions, network retries, client double-clicks, or malicious replays must never result in duplicate balance credits, invoice double-marking, or corrupted accounting states.

---

## 2. Idempotency Architecture & Double-Checked Locking
RentFlow AI implements a defense-in-depth, three-tier idempotency architecture:

```
                  +---------------------------------------------------+
                  |  1. Fast-Path Pre-Check (Read Uncommitted/Comm.)  |
                  |     findByTenantIdAndTransactionReference         |
                  +-------------------------+-------------------------+
                                            |
                         Already Exists? ---+--- Yes ---> Validate Replay vs Conflict
                                            |             (Returns 200 OK with cached PaymentDTO)
                                            No
                                            v
                  +---------------------------------------------------+
                  |  2. Pessimistic Write Locking (PESSIMISTIC_WRITE) |
                  |     Row Lock: Booking -> Invoice (Fixed Order)    |
                  +-------------------------+-------------------------+
                                            v
                  +---------------------------------------------------+
                  |  3. Re-Check Under Lock (Double-Checked Locking)  |
                  |     findByTenantIdAndTransactionReference         |
                  +-------------------------+-------------------------+
                                            |
                         Committed during --+--- Yes ---> Validate Replay vs Conflict
                         Lock Wait?                       (Returns 200 OK with cached PaymentDTO)
                                            |
                                            No
                                            v
                  +---------------------------------------------------+
                  |  4. Authoritative DB Unique Constraint            |
                  |     uq_payment_tenant_transaction_reference       |
                  |     (DataIntegrityViolationException Trap)        |
                  +---------------------------------------------------+
```

### 2.1 Fast-Path Pre-Check
Before acquiring row locks, `PaymentService.recordPayment` performs a tenant-scoped lookup for `transactionReference`. If a completed payment already exists:
- If parameters match (amount, booking, invoice, payment method), it returns the existing payment DTO with `idempotentReplay: true` (HTTP 200 OK).
- If any critical parameter conflicts (e.g. same reference used with a different amount or different booking), an `IdempotencyConflictException` is thrown (mapped to HTTP 409 Conflict).

### 2.2 Row-Level Locking & Deadlock Prevention
To prevent race conditions between concurrent requests targeting the same booking/invoice:
- The `Booking` row is locked first via `bookingRepository.findByIdAndTenantIdForUpdate(tenantId, bookingId)`.
- The `Invoice` row (if present) is locked second via `invoiceRepository.findByTenantIdAndBookingIdForUpdate(tenantId, bookingId)`.
- **Lock ordering is strictly deterministic across the entire codebase (`Booking` -> `Invoice`)**, ensuring that circular wait deadlocks are mathematically impossible.

### 2.3 Re-Check Under Lock (Double-Checked Locking)
When 10 concurrent requests with the identical transaction reference arrive at the same millisecond:
1. Thread 1 acquires the row lock, finds no payment, creates the payment, updates balances, and commits.
2. Threads 2 through 10 are queued on the row lock.
3. As each thread acquires the row lock, it executes the **re-check under lock**. It immediately finds Thread 1's committed payment, validates the parameters, and returns `idempotentReplay: true` without recalculating balances or throwing overpayment errors.

### 2.4 Authoritative DB Unique Constraint
In the event of distributed racing across disconnected cluster nodes or distributed transactions:
`uq_payment_tenant_transaction_reference UNIQUE (tenant_id, transaction_reference)` guarantees that the database engine rejects duplicate insertions at the storage engine level. `PaymentService` catches `DataIntegrityViolationException`, re-queries the authoritative committed record, validates replay compatibility, and safely returns the existing payment.

---

## 3. Financial Invariants & State Synchronization

### 3.1 Overpayment Prevention
Financial balances are calculated dynamically from authoritative locked state:
$$\text{Outstanding Balance} = \text{Total Amount} - \sum (\text{COMPLETED Payments})$$
If $\text{Payment Amount} > \text{Outstanding Balance}$, the transaction is rejected with an `IllegalArgumentException` ("Payment exceeds outstanding balance of $X.XX").

### 3.2 Atomic Multi-Entity Synchronization
Inside a single `@Transactional` boundary:
1. `Payment` record is persisted.
2. `Booking.depositPaid` is incremented.
3. `Booking.balanceDue` is decremented:
   - If $\text{balanceDue} \le 0 \implies \text{BookingStatus} = \text{PAID}$
   - If $\text{balanceDue} < \text{totalAmount} \implies \text{BookingStatus} = \text{PARTIALLY\_PAID}$
4. Associated `Invoice` is synchronized:
   - `Invoice.amountPaid` is updated.
   - `Invoice.balanceDue` is updated.
   - `InvoiceStatus` transitions to `PAID` or `PARTIALLY_PAID`.
5. Audit log entry is recorded in `PaymentAudit`.
6. Domain event `PaymentReceivedEvent` is dispatched to the Outbox.

If any step fails, the entire transaction rolls back cleanly, leaving balances and payments untouched.

---

## 4. Multi-Tenant Isolation & Customer IDOR Protection
- **Tenant Scoping**: All queries, locks, and constraints are strictly scoped by `tenantId`. Transaction references can be reused across different tenants without collision, but are strictly unique within each tenant.
- **Customer IDOR Protection**: When payments are initiated through customer-facing endpoints (or with role `CUSTOMER`), the server derives the authenticated `customerId` from the JWT token and verifies `customerId.equals(booking.getCustomerId())`. A customer can never pay, view, or affect another customer's invoice or booking.

---

## 5. Day 38 Flyway Migration Requirements
The JPA entity `Payment` defines table-level constraints for automated schema validation. During Day 38 production migration, the following migration script must be applied:

```sql
-- Day 38 Production Migration: Payment Idempotency & Financial Integrity
ALTER TABLE payment 
    ADD CONSTRAINT uq_payment_tenant_transaction_reference 
    UNIQUE (tenant_id, transaction_reference);

CREATE INDEX idx_payment_invoice 
    ON payment (tenant_id, invoice_id);

CREATE INDEX idx_payment_tenant_booking 
    ON payment (tenant_id, booking_id);
```

---

## 6. Frontend Protections
1. **Automated Reference Generation**: In `invoice-detail.component.ts` and `booking-detail.component.ts`, a client idempotency key is automatically generated when opening payment dialogs (`INV-PAY-<uuid>` or `BOOK-PAY-<uuid>`).
2. **Double-Click & Concurrent Submit Prevention**: The UI disables submission buttons and activates loading spinners (`isRecordingPayment = true`) immediately upon the first click.
3. **HTTP 409 Conflict Handling**: Dedicated catch handler alerts the user if an idempotency conflict occurs, advising them to refresh or generate a new transaction reference.
