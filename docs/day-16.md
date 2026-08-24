# Day 16: Inventory Availability, Inventory Reservation & Conflict Management

## 1. Objective
The Day 16 module introduces a high-concurrency, real-time **Inventory Availability, Reservation & Conflict Management Engine** for RentFlow AI. It guarantees that equipment is never double-booked or overpromised across Quotes, Bookings, Storefront, and Warehouse Operations.

Core operational workflow:

```
CUSTOMER / SALES → SELECT EVENT DATE → CHECK AVAILABILITY
   │
   ├─► AVAILABLE? (YES) ──► ATOMIC RESERVATION ──► BOOKING ──► WAREHOUSE PICK ──► DELIVERY
   │
   └─► AVAILABLE? (NO)  ──► CONFLICT DETECTED ──► SHOW SHORTAGE ──► SUGGEST ALTERNATIVES
```

---

## 2. Existing Inventory Architecture Inspection

### Reused Entities & Tables (Zero Duplication)
- **`Product`** ([`Product.java`](file:///c:/dev/rentflow-ai/backend/src/main/java/com/rentflow/ai/model/Product.java)): Holds master product records, SKU, tenant ID, `quantityOwned`, `quantityInMaintenance`, `quantityDamaged`, and `quantityLost`.
- **`InventoryReservation`** ([`InventoryReservation.java`](file:///c:/dev/rentflow-ai/backend/src/main/java/com/rentflow/ai/model/InventoryReservation.java)): Holds date-bounded unit reservations (`tenantId`, `productId`, `bookingId`, `eventId`, `quantity`, `startDateTime`, `endDateTime`, `status`, `reservationType`, `createdBy`).
- **`Booking` & `BookingItem`**: Contains rental line items with start/end datetimes.
- **`WarehouseOrder` & `WarehouseOrderItem`**: Links picked/packed status to reserved inventory.
- **`WarehouseLocation`**: Maps physical storage bays/shelves.

---

## 3. Availability Calculation Formula

Available inventory for a given product within a requested date/time window $[T_{\text{start}}, T_{\text{end}}]$ is calculated dynamically on the backend:

$$\text{Available} = \text{QuantityOwned} - \text{QuantityInMaintenance} - \text{QuantityDamaged} - \text{QuantityLost} - \sum \text{QuantityReserved}(T_{\text{start}}, T_{\text{end}})$$

Where $\sum \text{QuantityReserved}(T_{\text{start}}, T_{\text{end}})$ sums all active reservations (`CONFIRMED`, `RESERVED`, `PENDING`, `HOLD`) that temporally overlap with the requested period.

---

## 4. Reservation Model & Lifecycle

### Statuses
- **`PENDING`**: Temporary hold or unconfirmed draft reservation.
- **`CONFIRMED` / `RESERVED`**: Active reservation bound to a confirmed booking.
- **`HOLD`**: Short-term timed hold (e.g., 30-minute quote window).
- **`RELEASED`**: Released reservation due to booking completion or manual user action.
- **`CANCELLED`**: Cancelled reservation due to booking cancellation.
- **`EXPIRED`**: Timed hold that exceeded its expiration duration without booking confirmation.

### Reservation Types
- `BOOKING`: Tied to customer booking.
- `HOLD`: Temporary quote hold.
- `MAINTENANCE`: Internal block for service/repair.
- `OTHER`: Manual override or administrative buffer.

---

## 5. Reliable Date/Time Overlap Logic

Two rental time windows $[S_1, E_1]$ and $[S_2, E_2]$ overlap if and only if:

$$\text{Overlaps} \iff S_1 < E_2 \quad \text{AND} \quad E_1 > S_2$$

This logic is executed natively in database queries (`InventoryReservationRepository.findOverlappingReservations`):
```sql
SELECT r FROM InventoryReservation r
WHERE r.tenantId = :tenantId
  AND r.productId = :productId
  AND r.status IN ('RESERVED', 'PENDING', 'CONFIRMED', 'HOLD')
  AND r.startDateTime < :requestedEnd
  AND r.endDateTime > :requestedStart
```

---

## 6. Concurrency Strategy & Overbooking Prevention

To handle high-concurrency requests (e.g. Customer A and Customer B simultaneously requesting 100 units from a total pool of 150):
1. **Database Locking**: Uses `@Lock(LockModeType.PESSIMISTIC_WRITE)` or synchronized transactional checks (`@Transactional(isolation = Isolation.SERIALIZABLE)`) during reservation creation.
2. **Atomic Validation**: Availability is re-checked inside the isolated transaction before saving new `InventoryReservation` records. If requested quantity exceeds remaining available quantity, the transaction rolls back and returns a HTTP 409 Conflict / HTTP 400 Bad Request error:
   > *"Only 50 units are available for the selected rental period."*

---

## 7. REST APIs Introduced / Enhanced

| Method | Endpoint | Description |
| :--- | :--- | :--- |
| `GET` | `/api/inventory/availability` | Single product availability check for a date range |
| `POST` | `/api/inventory/availability/check` | Bulk multi-product cart availability validation |
| `POST` | `/api/inventory/reservations` | Atomic inventory reservation creation |
| `GET` | `/api/inventory/reservations` | List active & historical reservations |
| `GET` | `/api/inventory/reservations/{id}` | Get single reservation details |
| `POST` | `/api/inventory/reservations/{id}/release` | Manually release a reservation |
| `GET` | `/api/inventory/conflicts` | List active inventory shortages & overbooking conflicts |
| `GET` | `/api/inventory/dashboard` | Consolidated metrics dashboard for inventory |
| `GET` | `/api/inventory/products/{productId}/availability` | Daily availability timeline for a specific product |
| `GET` | `/api/inventory/products/{productId}/alternatives` | Category-based alternative product recommendations |

---

## 8. Angular UI Components

1. **`InventoryDashboardComponent`** (`/inventory/dashboard`): Metric cards (Total Units, Available, Reserved, Out on Rent, Maintenance, Damaged, Conflicts), low stock alerts, and quick actions.
2. **`InventoryAvailabilityCalendarComponent`** (`/inventory/availability`): Date picker & table showing real-time total, reserved, and available counts for all rental products.
3. **`InventoryReservationsListComponent`** (`/inventory/reservations`): Filterable, searchable table of reservations.
- **`InventoryProductAvailabilityComponent`** (`/inventory/products/:id/availability`): Product-specific daily availability timeline grid.
- **`InventoryConflictsComponent`** (`/inventory/conflicts`): Conflict resolution screen displaying shortages with booking links and alternative equipment suggestions.

---

## 9. RBAC & Tenant Isolation

- **Tenant Isolation**: Every database query filters strictly by `X-Tenant-Id`. Tenant A's inventory pool and reservations are completely invisible and isolated from Tenant B.
- **RBAC Roles**:
  - `OWNER` / `ADMIN`: Full access (check, reserve, release, adjust, view conflicts).
  - `SALES`: Availability checks, quote holds, reservation creation. Cannot directly adjust inventory levels.
  - `WAREHOUSE_MANAGER` / `WAREHOUSE_OPERATOR`: Read-only availability & reservation lookup.
  - `FINANCE`: Read-only access.
  - `CUSTOMER`: Sanitized availability check (only returns true/false & quantity available; hides internal rack locations, supplier notes, and other customers' booking details).

---

## 10. Audit & Notifications

- **Audit Events**: `INVENTORY_AVAILABILITY_CHECKED`, `INVENTORY_RESERVED`, `INVENTORY_RESERVATION_RELEASED`, `INVENTORY_RESERVATION_CANCELLED`, `INVENTORY_CONFLICT_DETECTED`.
- **Notifications**: Triggers `NotificationService` alerts when an inventory conflict is detected (`INVENTORY_CONFLICT_DETECTED`) or when a quote hold expires.

---

## 11. Known Limitations & Future AI Integration

- **Day 16 Focus**: Focuses strictly on quantity-based rental availability, date/time overlap calculation, atomic reservation transactions, and conflict detection.
- **Future AI Capabilities**: Prepares `InventoryAlternativeService` for future AI copilot recommendations (e.g. *"Suggesting White Folding Chairs as alternative for 20 missing Chiavari Chairs"*). Automated autonomous reservation without human approval is explicitly restricted.
