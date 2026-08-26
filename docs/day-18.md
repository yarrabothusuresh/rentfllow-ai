# Day 18 Architecture & Specification — Returns, Pickup, Check-In & Inspection Lifecycle

## 1. Objective
The objective of Day 18 is to complete the rental return lifecycle for RentFlow AI SaaS. It extends the logistics pipeline from delivery completion (`OUT_ON_RENT` / `DELIVERED`) through pickup scheduling, driver and vehicle assignment, mobile driver execution, warehouse equipment check-in, item-by-item condition inspection, damage recording, inventory reconciliation (`AVAILABLE`, `MAINTENANCE`, `DAMAGED`, `LOST`), booking state update (`RETURNED`), customer portal visibility, and audit logging.

---

## 2. Existing Architecture Findings

| Component | Existing Model/Service | Reuse & Extension Strategy |
| :--- | :--- | :--- |
| **Tenant Isolation** | `tenantId` indexed across all entities | Multi-tenancy enforced on every return order, return item, inspection, damage record, and query. |
| **Booking & Customer** | `Booking`, `BookingItem`, `Customer`, `Event` | Returns created from delivered/out-on-rent bookings. Snapshots of customer and product SKU/names stored on return items. |
| **Logistics (Day 17)** | `Driver`, `Vehicle`, `Delivery` | Reuses Day 17 `Driver` and `Vehicle` entities and availability rules. Cross-validates time overlaps between Deliveries and Pickups. |
| **Inventory System** | `Product`, `InventoryReservation`, `InventoryTransaction` | Updates product inventory state (`quantityOwned`, `quantityInMaintenance`, `quantityDamaged`, `quantityLost`) transactionally. Releases reservations upon return completion. |
| **Notifications** | `NotificationService` | Sends automated internal and customer notifications (`RETURN_SCHEDULED`, `RETURN_ASSIGNED`, `RETURN_PICKUP_STARTED`, `RETURN_PICKED_UP`, `RETURN_READY_FOR_INSPECTION`, `RETURN_COMPLETED`, `RETURN_ITEM_MISSING`, `RETURN_ITEM_DAMAGED`). |
| **Audit Log** | `ReturnAudit`, `DeliveryAudit` | Detailed operational audit trails recorded for all return, check-in, inspection, and inventory transition events. |

---

## 3. Rental Return Lifecycle & State Machine

```
BOOKING (DELIVERED / OUT_ON_RENT)
   ↓
RETURN ORDER CREATED (PENDING)
   ↓
SCHEDULED & DRIVER/VEHICLE ASSIGNED (SCHEDULED / ASSIGNED)
   ↓
OUT FOR PICKUP (Driver en route)
   ↓
ARRIVED AT VENUE
   ↓
PICKED UP (Equipment collected from customer)
   ↓
CHECK-IN (Warehouse receives equipment & tallies received vs expected)
   ↓
INSPECTION (Warehouse inspects condition: Good / Damaged)
   ↓
COMPLETED (Inventory updated: Good → AVAILABLE, Damaged → MAINTENANCE/DAMAGED, Missing → LOST)
```

### Allowed Return Status Transitions
- `PENDING` → `SCHEDULED`
- `SCHEDULED` → `ASSIGNED`, `CANCELLED`
- `ASSIGNED` → `READY_FOR_PICKUP`, `OUT_FOR_PICKUP`, `CANCELLED`
- `READY_FOR_PICKUP` → `OUT_FOR_PICKUP`, `CANCELLED`
- `OUT_FOR_PICKUP` → `ARRIVED`, `FAILED`
- `ARRIVED` → `PICKED_UP`, `FAILED`
- `PICKED_UP` → `CHECK_IN`
- `CHECK_IN` → `INSPECTION`
- `INSPECTION` → `COMPLETED`
- Terminal states: `COMPLETED`, `CANCELLED`, `FAILED`

---

## 4. Domain Data Models

### 4.1 ReturnOrder (`com.rentflow.returns.model.ReturnOrder`)
- `id` (UUID)
- `tenantId` (String)
- `returnNumber` (String, e.g. `RET-000001`)
- `bookingId` (UUID)
- `deliveryId` (UUID, optional)
- `customerId` (UUID)
- `eventId` (UUID, optional)
- `status` (`ReturnOrderStatus`)
- `priority` (`ReturnPriority`)
- `scheduledDate` (LocalDate)
- `scheduledStartTime` (String, e.g. `10:00`)
- `scheduledEndTime` (String, e.g. `12:00`)
- `pickupAddressSnapshot` (String)
- `driverId` (UUID, optional)
- `vehicleId` (UUID, optional)
- `notes` (String)
- `actualPickupStartTime` (LocalDateTime)
- `actualArrivalTime` (LocalDateTime)
- `actualPickupTime` (LocalDateTime)
- `actualCheckInTime` (LocalDateTime)
- `actualInspectionTime` (LocalDateTime)
- `completedAt` (LocalDateTime)
- `createdAt`, `updatedAt` (LocalDateTime)

### 4.2 ReturnOrderItem (`com.rentflow.returns.model.ReturnOrderItem`)
- `id` (UUID)
- `tenantId` (String)
- `returnOrderId` (UUID)
- `bookingItemId` (UUID)
- `productId` (UUID)
- `productNameSnapshot` (String)
- `skuSnapshot` (String)
- `quantityExpected` (int)
- `quantityReceived` (int)
- `quantityMissing` (int)
- `quantityDamaged` (int)
- `quantityGood` (int)
- `status` (`ReturnOrderItemStatus`: `PENDING`, `PARTIAL`, `RECEIVED`, `MISSING`, `DAMAGED`, `INSPECTED`)
- `notes` (String)
- `createdAt`, `updatedAt` (LocalDateTime)

### 4.3 Inspection (`com.rentflow.returns.model.Inspection`)
- `id` (UUID)
- `tenantId` (String)
- `returnOrderId` (UUID)
- `returnOrderItemId` (UUID)
- `productId` (UUID)
- `inspectedQuantity` (int)
- `goodQuantity` (int)
- `damagedQuantity` (int)
- `missingQuantity` (int)
- `condition` (`InspectionCondition`: `GOOD`, `MINOR_DAMAGE`, `MAJOR_DAMAGE`, `UNUSABLE`)
- `notes` (String)
- `inspectedBy` (String)
- `inspectedAt` (LocalDateTime)

### 4.4 DamageRecord (`com.rentflow.returns.model.DamageRecord`)
- `id` (UUID)
- `tenantId` (String)
- `inspectionId` (UUID)
- `returnItemId` (UUID)
- `productId` (UUID)
- `quantity` (int)
- `category` (`DamageCategory`: `BROKEN`, `STAINED`, `SCRATCHED`, `MISSING_PART`, `WATER_DAMAGE`, `OTHER`)
- `severity` (`DamageSeverity`: `MINOR`, `MAJOR`, `CRITICAL`)
- `description` (String)
- `estimatedRepairCost` (BigDecimal)
- `estimatedReplacementCost` (BigDecimal)
- `status` (String: `OPEN`, `IN_REPAIR`, `RESOLVED`)
- `createdAt` (LocalDateTime)

### 4.5 ReturnAudit (`com.rentflow.returns.model.ReturnAudit`)
- `id` (UUID)
- `tenantId` (String)
- `returnOrderId` (UUID)
- `action` (String)
- `performedBy` (String)
- `details` (String)
- `createdAt` (LocalDateTime)

---

## 5. Scheduling & Assignment Rules

1. **Driver Overlap Rule**: A driver assigned to a DELIVERY (e.g. 10 AM – 12 PM) cannot be assigned to a PICKUP (ReturnOrder) during an overlapping time window.
2. **Vehicle Overlap Rule**: A vehicle assigned to a DELIVERY cannot be assigned to a PICKUP during an overlapping window.
3. **Check-In Rule**: `quantityReceived` cannot exceed `quantityExpected`. `quantityMissing` is computed as `quantityExpected - quantityReceived`.
4. **Inspection Rule**: `goodQuantity + damagedQuantity` must equal `quantityReceived`.

---

## 6. Inventory State Reconciliation upon Return Completion

Upon completing a return:
- **Good Items (`goodQuantity`)** → Returned to `AVAILABLE`. Reservation released.
- **Damaged Items (`damagedQuantity`)** → Marked `MAINTENANCE` or `DAMAGED`. Product `quantityInMaintenance` or `quantityDamaged` incremented. Reservation released.
- **Missing Items (`quantityMissing`)** → Marked `LOST`. Product `quantityLost` incremented. Reservation released.
- **Booking Status** → Updated to `RETURNED`.

---

## 7. Customer Portal & Customer-Safe Receipt

- **Customer View**: Shows high-level rental return status (`RETURNED`), pickup window, expected vs returned item counts.
- **Sanitization**: Hides internal inspection notes, damage assessments, repair/replacement cost estimates, and warehouse operator notes.

---

## 8. REST APIs

- `POST /api/returns/from-booking/{bookingId}`
- `GET /api/returns`
- `GET /api/returns/{id}`
- `PATCH /api/returns/{id}/schedule`
- `PATCH /api/returns/{id}/driver`
- `PATCH /api/returns/{id}/vehicle`
- `POST /api/returns/{id}/start`
- `POST /api/returns/{id}/arrive`
- `POST /api/returns/{id}/pickup-complete`
- `POST /api/returns/{id}/start-check-in`
- `POST /api/returns/{id}/check-in`
- `POST /api/returns/{id}/start-inspection`
- `POST /api/returns/{id}/inspection`
- `POST /api/returns/{id}/complete`
- `GET /api/returns/dashboard`
- `GET /api/returns/inspection`
- `GET /api/inventory/damage`
- `GET /api/returns/{id}/summary`

---

## 9. RBAC & Security Matrix

| Role | Access Level |
| :--- | :--- |
| **OWNER / ADMIN** | Full access to returns, inspections, damage, and inventory adjustments. |
| **OPERATIONS_MANAGER** | Schedule pickup, assign driver/vehicle, manage returns, approve return completion. |
| **WAREHOUSE_MANAGER** | Execute check-in, perform inspection, update inventory condition. |
| **WAREHOUSE_OPERATOR** | Perform check-in and inspection. |
| **DRIVER** | View assigned pickups, start pickup, mark arrival, confirm pickup. |
| **SALES / FINANCE** | Read-only return and damage visibility. |
| **CUSTOMER** | Read-only access to customer-safe return status and summary. |

---

## 10. AI Preparation
- `InspectionRecommendationService` abstraction defined to provide AI recommendation hooks (condition assessment and damage categorization) without external vision API dependencies.
