# Day 37 — Inventory Concurrency Regression & Reservation Integrity Architecture

## 1. Concurrency Control Architecture (Section 8)

RentFlow AI implements **Targeted Pessimistic Row-Level Locking** (`PESSIMISTIC_WRITE`) combined with strict isolation and atomic transaction boundaries for all inventory reservation and booking conversion operations.

### Lock Mechanism Details
- **Lock Target:** `Product` entity (`products` table) via `ProductRepository.findWithLockByTenantIdAndId(tenantId, productId)`.
- **JPA Lock Mode:** `LockModeType.PESSIMISTIC_WRITE` translating in PostgreSQL to `SELECT ... FOR UPDATE` with `tenant_id` predicate binding.
- **Transaction Demarcation:** Spring `@Transactional(isolation = Isolation.READ_COMMITTED)` on `BookingService.createBookingFromQuote`, `BookingService.confirmBooking`, and `InventoryService.createBatchReservations`.
- **Lock Scope:** Product rows are locked exclusively for the duration of the reservation creation and availability check transaction. No global table locks or full-tenant locks are ever acquired.
- **Lock Ordering:** Locks are ALWAYS acquired in canonical ascending UUID order (`.distinct().sorted()`), mathematically eliminating circular wait conditions and preventing PostgreSQL deadlocks on multi-product quotes.

---

## 2. Date-Interval Availability Math (Section 9)

Rental reservations are time-windowed assets rather than static inventory counters. Availability is computed as:

$$\text{Net Available} = \text{Quantity Owned} - (\text{Damaged} + \text{Maintenance} + \text{Lost}) - \sum_{\text{overlapping}} \text{Reserved Quantity}$$

### Overlap Query Logic
In `InventoryReservationRepository.findConflictingReservations`:
```sql
SELECT r FROM InventoryReservation r
WHERE r.tenantId = :tenantId
  AND r.productId = :productId
  AND r.status IN ('CONFIRMED', 'ACTIVE')
  AND r.startDateTime < :requestedEnd
  AND r.endDateTime > :requestedStart
```

### Boundary & Turnaround Rules
- **Non-Overlapping Contiguous Rentals:** When turnaround is zero, a booking ending at 12:00:00 does not conflict with a booking starting at 12:00:00 because `12:00:00 > 12:00:00` is `FALSE`.
- **Turnaround Buffer Extension:** When a product has `defaultTurnaroundMinutes > 0`, `AvailabilityService` expands the requested evaluation window:
  - $\text{Effective Start} = \text{Requested Start} - \text{Turnaround Buffer}$
  - $\text{Effective End} = \text{Requested End} + \text{Turnaround Buffer}$
  This ensures warehouse crews have sufficient inspection and staging time between bookings, preventing overlapping handovers.

---

## 3. Canonical Lock Ordering & Deadlock Avoidance (Section 10)

When concurrent bookings reserve multiple overlapping products, arbitrary acquisition orders lead to database deadlocks (PostgreSQL error `40P01: deadlock detected`).

### Mathematical Deadlock Prevention Proof
Let product IDs be elements of an ordered set $(\mathcal{U}, \le)$, where $\mathcal{U}$ is the UUID universe with standard lexicographical comparison.

1. Any transaction $T_i$ acquiring locks on products $\{p_{i,1}, p_{i,2}, \dots, p_{i,k}\}$ sorts the set such that:
   $$p_{i,1} < p_{i,2} < \dots < p_{i,k}$$
2. Assume a deadlock cycle exists among transactions $T_1, T_2, \dots, T_n$:
   - $T_1$ holds $A$ and waits for $B$ (held by $T_2$) $\implies A < B$
   - $T_2$ holds $B$ and waits for $C$ (held by $T_3$) $\implies B < C$
   - ...
   - $T_n$ holds $N$ and waits for $A$ (held by $T_1$) $\implies N < A$
3. By transitivity: $A < B < C < \dots < N < A \implies A < A$, which is a contradiction under strict total ordering.
4. Therefore, circular wait is impossible and deadlocks cannot occur.

### Implementation
Both `BookingService` and `InventoryService.createBatchReservations` execute:
```java
List<UUID> orderedProductIds = items.stream()
    .map(Item::getProductId)
    .filter(Objects::nonNull)
    .distinct()
    .sorted()
    .toList();
for (UUID pId : orderedProductIds) {
    productRepository.findWithLockByTenantIdAndId(tenantId, pId)
        .orElseThrow(() -> new IllegalArgumentException("Product not found or access denied: " + pId));
}
```

---

## 4. Idempotent Booking Conversion (Section 11)

To prevent duplicate bookings from network retries, browser double-clicks, or replay attacks:

1. **Double-Checked Locking Under Transaction Lock:**
   Inside the locked transaction in `BookingService.createBookingFromQuote`:
   ```java
   bookingRepository.findByTenantIdAndQuoteId(tenantId, quoteId).ifPresent(existing -> {
       throw new IllegalStateException("Quote already converted to booking: " + existing.getBookingNumber());
   });
   ```
2. **Database Unique Constraint Enforcement:**
   `Booking` entity enforces:
   ```java
   @Table(name = "booking", uniqueConstraints = {
       @UniqueConstraint(name = "uq_booking_tenant_quote", columnNames = {"tenantId", "quoteId"})
   })
   ```
   If two requests race before the initial lookup, the PostgreSQL unique index forces the second transaction to abort with a DataIntegrityViolationException, preventing duplicate bookings or double reservations.

---

## 5. Cross-Tenant Isolation (Section 15)

RentFlow enforces tenant boundary isolation across four layers:
1. **Row Locks:** `findWithLockByTenantIdAndId(tenantId, productId)` explicitly binds `tenantId`. If Tenant A passes Tenant B's product ID, the query returns `Optional.empty()` and immediately aborts with `400/403 Product not found or access denied`.
2. **Reservations:** Every `InventoryReservation` entity records `tenantId` and is queried strictly with `r.tenantId = :tenantId`.
3. **RBAC Controls:** `InventoryController` `/receive` and `/adjust` endpoints enforce roles (`OWNER`, `ADMIN`, `WAREHOUSE`) via `SecurityUtils.getCurrentUserRole()`.
4. **Targeted Lookups:** Full-tenant table scans (`findByTenantId(...)`) have been completely replaced with composite index lookups (`findByTenantIdAndBookingId(tenantId, bookingId)`).

---

## 6. Atomic Rollback & State Machine Integrity (Section 69)

### All-or-Nothing Multi-Item Booking
If a quote requests Item 1 (available) and Item 2 (out of stock), the transaction evaluates availability after acquiring all product locks. Upon encountering shortage on Item 2, an `InventoryUnavailableException` is thrown. Spring's transaction manager rolls back the entire database transaction:
- Zero reservations are persisted for Item 1 or Item 2.
- Zero bookings or booking items are persisted.
- Quote status remains unmodified.

### Idempotent Cancellation & Returns
- When `BookingService.cancelBooking` is invoked, it checks `booking.getStatus() == BookingStatus.CANCELLED`. If already cancelled, it returns immediately without modifying stock or reservations.
- If not cancelled, it transitions reservations to `CANCELLED` and marks the booking `CANCELLED`. Subsequent calls are harmless no-ops.
- Returning items via `ReturnService` operates strictly on `findByTenantIdAndBookingId`, transitioning active reservations to `COMPLETED` without inflating physical inventory counts (`quantityOwned`).
