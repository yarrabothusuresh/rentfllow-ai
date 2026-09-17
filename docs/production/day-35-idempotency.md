# RentFlow AI — Day 35: Booking, Checkout & Rental Request Idempotency

## 1. Overview & Core Production Invariant

In high-concurrency SaaS rental platforms, unreliable networks, user double-clicks, browser back/refresh cycles, and concurrent automated systems create duplicate requests. 

The core operational invariant of RentFlow AI is:
> **A single logical business operation must produce exactly one business effect across all downstream systems, even when submitted repeatedly or concurrently.**

A replayed request with an identical payload must return the original resource representation with an indicator of idempotent replay (`idempotentReplay: true`, HTTP 200/201). A replayed request with a modified or conflicting payload using the same key must be rejected immediately with **HTTP 409 Conflict** and a descriptive explanation.

---

## 2. Seven Idempotency Points

RentFlow AI guarantees end-to-end idempotency across the seven critical stages of the rental lifecycle:

```
[Public Storefront Checkout] 
         │ (HTTP POST with Idempotency-Key header)
         ▼
[Rental Request Creation] ──────────► [DB: uq_rental_req_tenant_idempotency]
         │ (CustomerRequestCreatedEvent)
         ▼
[CRM Lead Creation] ────────────────► [DB: uq_crm_lead_tenant_rental_req]
         │
         ▼
[Quote Generation] ─────────────────► [Index: idx_quotes_tenant_idempotency]
         │ (Customer Accepts Quote)
         ▼
[Quote-to-Booking Conversion] ──────► [DB: uq_booking_tenant_quote]
         │ (Pessimistic Row Locks in Canonical Order)
         ▼
[Inventory Reservation] ────────────► [Atomic Allocation Under Product Lock]
```

### 2.1 Public Storefront Checkout
- **Path**: `POST /api/public/rental-requests`
- **Mechanism**: Accepts `Idempotency-Key` HTTP header (or body field). The storefront Angular checkout generates a UUIDv4 on component initialization and submits it with each attempt.
- **Result**: Resubmitting the same checkout returns the original cached rental request.

### 2.2 Rental Request Creation
- **Service**: `RentalRequestService.createRentalRequest`
- **Constraint**: `uq_rental_req_tenant_idempotency` on `rental_requests(tenant_id, idempotency_key)`.
- **Integrity**: Deterministic SHA-256 fingerprint hash (`requestHash`) is computed and stored. Mismatched payloads return HTTP 409 Conflict.
- **Concurrency**: Fast-path DB query combined with in-process synchronized lock and `TransactionTemplate` ensures that concurrent requests commit sequentially and return the cached resource.

### 2.3 Guest Customer Creation
- **Service**: `PublicQuoteRequestService.findOrCreateCustomer`
- **Mechanism**: Case-insensitive lookup by email and phone within the tenant domain. Re-uses the existing customer entity rather than creating duplicates.

### 2.4 CRM Lead Creation
- **Listener**: `CrmEventListener.onCustomerRequestCreated`
- **Constraint**: `uq_crm_lead_tenant_rental_req` on `crm_leads(tenant_id, rental_request_id)`.
- **Integrity**: Replay of duplicate events (or concurrent event delivery across threads) creates exactly 1 lead and 1 lead activity record.

### 2.5 Quote Generation
- **Service**: `QuoteService.createQuote` & `PublicQuoteRequestService.submitQuoteRequest`
- **Index**: `idx_quotes_tenant_idempotency` on `quotes(tenant_id, idempotency_key)`.
- **Integrity**: Prevents accidental duplication of auto-generated quotes while maintaining flexibility for legitimate revisions.

### 2.6 Quote-to-Booking Conversion
- **Service**: `BookingService.convertQuoteToBooking`
- **Constraint**: `uq_booking_tenant_quote` on `booking(tenant_id, quote_id)`.
- **Double-Checked Locking**:
  1. Initial check on `bookingRepository.findByTenantIdAndQuoteId`.
  2. Row-level write locks acquired on all required products in canonical sorted order (`PESSIMISTIC_WRITE`).
  3. Second check under lock: if another concurrent thread completed conversion, returns the existing booking immediately.
  4. Constraint violation fallback traps race conditions across distributed cluster instances.
- **Cancellation Invariant**: Once a booking converted from a quote has been CANCELLED, that quote cannot be reconverted. Attempting conversion throws `IllegalStateException`.

### 2.7 Inventory Reservation
- **Protection**: Executed exclusively within the product write locks of `convertQuoteToBooking`.
- **Guarantee**: Competing conversions cannot oversell stock or duplicate reservation line items.

---

## 3. Canonical Fingerprinting & SHA-256 Tamper Protection

RentFlow AI computes a canonical hash of all business-defining parameters using `com.rentflow.common.idempotency.IdempotencyUtils`:

```java
String currentHash = IdempotencyUtils.computeRequestFingerprint(
    tenantId,
    customerEmail,
    rentalStartDate,
    rentalEndDate,
    canonicalItems, // Sorted list of "productId:quantity"
    estimatedTotal
);
```

When an idempotency key is replayed:
- **Matching Hash**: Cached entity is returned with `idempotentReplay: true`.
- **Different Hash**: `IdempotencyConflictException` is thrown, returning HTTP 409 Conflict:
  ```json
  {
    "status": 409,
    "error": "Idempotency Conflict",
    "message": "This request has changed since it was first submitted. Please start a new checkout.",
    "timestamp": "2026-09-17T08:35:00Z"
  }
  ```

---

## 4. Database Constraints & Day 38 Flyway Migration

The authoritative state boundary resides in PostgreSQL database unique constraints.

### Flyway Migration Script
Location: `backend/src/main/resources/db/migration/V35__idempotency_constraints.sql`

```sql
-- 1. Rental Requests Idempotency & Tamper Protection
ALTER TABLE rental_requests
    ADD COLUMN IF NOT EXISTS idempotency_key VARCHAR(255),
    ADD COLUMN IF NOT EXISTS request_hash VARCHAR(64);

ALTER TABLE rental_requests
    ADD CONSTRAINT uq_rental_req_tenant_idempotency
    UNIQUE (tenant_id, idempotency_key);

CREATE INDEX IF NOT EXISTS idx_rental_req_tenant_idemp
    ON rental_requests (tenant_id, idempotency_key);

-- 2. CRM Lead Deduplication on Rental Request
ALTER TABLE crm_leads
    ADD CONSTRAINT uq_crm_lead_tenant_rental_req
    UNIQUE (tenant_id, rental_request_id);

-- 3. Booking Deduplication on Quote Conversion
ALTER TABLE booking
    ADD CONSTRAINT uq_booking_tenant_quote
    UNIQUE (tenant_id, quote_id);

-- 4. Quotes Idempotency Index
ALTER TABLE quotes
    ADD COLUMN IF NOT EXISTS idempotency_key VARCHAR(255);

CREATE INDEX IF NOT EXISTS idx_quotes_tenant_idempotency
    ON quotes (tenant_id, idempotency_key);
```

---

## 5. Frontend Idempotency Integration

The Angular storefront and customer portal provide defensive UX controls against double submissions:

1. **UUIDv4 Key Generation**:
   - `CheckoutComponent` generates an idempotency key upon component initialization.
   - `QuoteRequestComponent` generates an idempotency key on initialization.
2. **Button Disabled State**:
   - Primary submit buttons (`submitRentalRequest`, `submitQuoteRequest`) are immediately disabled when `isSubmitting = true`.
   - A visual spinner indicator replaces or accents button text during processing.
3. **HTTP Header Propagation**:
   - Requests are sent with the `Idempotency-Key` HTTP header.
   - CORS policy in `SecurityConfig.java` explicitly exposes and permits the `Idempotency-Key` header.
4. **Conflict Handling (HTTP 409)**:
   - If an idempotency conflict occurs (HTTP 409), the user is presented with an amber alert banner explaining that the request content changed, allowing them to start a fresh checkout with a new key.

---

## 6. Verification & Automated Test Suites

All 5 dedicated Day 35 test suites pass with 100% success rate:

| Test Suite | Test Count | Status | Scenarios Covered |
|:---|:---:|:---:|:---|
| `RentalRequestIdempotencyTest` | 5 | PASSED | Sequential replay (5x), 10-thread concurrency, conflict detection (409), distinct keys, tenant isolation |
| `LeadCreationIdempotencyTest` | 2 | PASSED | Event replay (5x) creates 1 lead, concurrent event delivery (10 threads) creates exactly 1 lead |
| `QuoteCreationIdempotencyTest` | 2 | PASSED | Identical quote request replay returns cached quote, revised quote creates new quote |
| `BookingConversionIdempotencyTest` | 3 | PASSED | 5x quote conversion returns same booking, reservations allocated once, cancelled booking conversion rejected |
| `BookingConcurrencyTest` | 1 | PASSED | 10 concurrent threads converting same quote create 1 booking, competing quotes respect inventory caps |

### Full Test Suite Regression
- **Backend**: `mvn test` executed **405 tests** with **0 failures, 0 errors, 0 skipped**.
- **Frontend**: Karma `ChromeHeadless` executed **18 tests** with **18 passed (0 failures)**.
- **Build**: `ng build` completed successfully in `frontend/dist/frontend`.
