# Day 17 Architecture & Specification — Delivery Management, Driver Assignment, Delivery Scheduling & Basic Route Planning

## 1. Objective
The objective of Day 17 is to introduce a complete **Delivery Management & Logistics Module** for RentFlow AI. It connects warehouse preparation (`PACKED` or `READY_FOR_DELIVERY`) to delivery creation, driver scheduling, vehicle capacity management, basic route planning, driver mobile execution, customer portal tracking, and operational audit trails.

---

## 2. Existing Architecture Findings

| Component | Existing Model/Service | Reuse & Extension Strategy |
| :--- | :--- | :--- |
| **Tenant Isolation** | `tenantId` indexed on all entities | Enforced on every delivery, driver, vehicle, and route query. |
| **Customer & Address** | `Customer`, `Event` | Address snapshots copied from `Event` venue address or `Customer` shipping address to prevent future edits from altering historical delivery records. |
| **Booking & Warehouse** | `Booking`, `WarehouseOrder` | Delivery created only from valid `WarehouseOrder` when status is `PACKED` or `READY_FOR_DELIVERY`. |
| **Inventory & Reservation**| `InventoryReservation` | Inventory remains reserved during delivery and transitions to `OUT_ON_RENT` upon completion. |
| **Users & Roles** | `User`, `RoleType` | Extends `RoleType.DRIVER`. Maps user account to `Driver` profile. |
| **Notifications** | `NotificationService` | Triggers events for `DELIVERY_SCHEDULED`, `DELIVERY_ASSIGNED`, `DELIVERY_STARTED`, `DELIVERY_ARRIVED`, `DELIVERY_COMPLETED`, and `DELIVERY_FAILED`. |
| **Audit Log** | `DeliveryAudit` | Operational audit logging for all delivery and route lifecycle actions. |

---

## 3. Core Delivery Workflow & State Machine

```
BOOKING
   ↓
INVENTORY RESERVED
   ↓
WAREHOUSE PICKED & PACKED (READY_FOR_DELIVERY)
   ↓
DELIVERY CREATED (PENDING)
   ↓
SCHEDULED & DRIVER/VEHICLE ASSIGNED (SCHEDULED / ASSIGNED)
   ↓
OUT FOR DELIVERY
   ↓
ARRIVED AT VENUE
   ↓
SETUP IN PROGRESS (if setup_required = true)
   ↓
DELIVERED (Inventory stays OUT_ON_RENT, Driver & Vehicle become AVAILABLE)
```

### Allowed State Transitions (`DeliveryStatusTransitionService`)
- `PENDING` → `SCHEDULED`
- `SCHEDULED` → `ASSIGNED`, `CANCELLED`
- `ASSIGNED` → `READY`, `OUT_FOR_DELIVERY`, `CANCELLED`
- `READY` → `OUT_FOR_DELIVERY`, `CANCELLED`
- `OUT_FOR_DELIVERY` → `ARRIVED`, `FAILED`
- `ARRIVED` → `SETUP_IN_PROGRESS`, `DELIVERED`, `FAILED`
- `SETUP_IN_PROGRESS` → `DELIVERED`, `FAILED`
- `DELIVERED`, `CANCELLED`, `FAILED` → Terminal states

---

## 4. Domain Data Models

### 4.1 Delivery (`com.rentflow.delivery.model.Delivery`)
- `id` (UUID)
- `tenantId` (String)
- `deliveryNumber` (String, e.g. `DEL-000001`)
- `bookingId` (UUID)
- `warehouseOrderId` (UUID)
- `customerId` (UUID)
- `eventId` (UUID)
- `deliveryType` (`DELIVERY`, `PICKUP`)
- `status` (`DeliveryStatus`)
- `priority` (`LOW`, `NORMAL`, `HIGH`, `URGENT`)
- `scheduledDate` (LocalDate)
- `scheduledStartTime` (String, e.g. `10:00`)
- `scheduledEndTime` (String, e.g. `12:00`)
- `deliveryAddressSnapshot` (String / JSON)
- `latitude` (Double)
- `longitude` (Double)
- `driverId` (UUID)
- `vehicleId` (UUID)
- `sequenceNumber` (Integer)
- `estimatedDistance` (Double)
- `estimatedDuration` (Integer, minutes)
- `actualStartTime` (LocalDateTime)
- `actualArrivalTime` (LocalDateTime)
- `actualCompletionTime` (LocalDateTime)
- `notes` (String)
- `customerNotes` (String)
- `setupRequired` (Boolean)
- `setupDurationMinutes` (Integer)
- `failureReason` (String)
- `failureNotes` (String)
- `createdAt`, `updatedAt` (LocalDateTime)

### 4.2 Driver (`com.rentflow.delivery.model.Driver`)
- `id` (UUID)
- `tenantId` (String)
- `userId` (UUID, optional link to AppUser)
- `name` (String)
- `phone` (String)
- `licenseNumber` (String)
- `status` (`AVAILABLE`, `ASSIGNED`, `ON_DELIVERY`, `OFF_DUTY`)
- `active` (Boolean)
- `createdAt`, `updatedAt` (LocalDateTime)

### 4.3 Vehicle (`com.rentflow.delivery.model.Vehicle`)
- `id` (UUID)
- `tenantId` (String)
- `vehicleNumber` (String, e.g. `VAN-01`)
- `name` (String, e.g. `Ford Transit`)
- `type` (String, e.g. `Van`, `Box Truck`)
- `capacity` (Integer, total equipment units)
- `active` (Boolean)
- `status` (`AVAILABLE`, `ASSIGNED`, `IN_USE`, `MAINTENANCE`, `INACTIVE`)
- `createdAt`, `updatedAt` (LocalDateTime)

### 4.4 DeliveryRoute & DeliveryRouteStop
- `DeliveryRoute`: `id`, `tenantId`, `routeNumber`, `driverId`, `vehicleId`, `routeDate`, `status`, `totalDeliveries`, `estimatedDistance`, `estimatedDuration`.
- `DeliveryRouteStop`: `id`, `routeId`, `deliveryId`, `sequenceNumber`, `estimatedArrival`, `actualArrival`, `status`.

---

## 5. Driver & Vehicle Assignment Rules

1. **Driver Overlap Rule**: A driver cannot be assigned to two deliveries with overlapping scheduled date and time windows (`StartA < EndB AND EndA > StartB`).
   - Error Response: `"Driver John Smith is already assigned to another delivery during this time."`
2. **Vehicle Overlap Rule**: A vehicle cannot be assigned to two deliveries with overlapping scheduled date and time windows.
   - Error Response: `"Vehicle VAN-01 is already assigned during this time."`
3. **Vehicle Capacity Rule**: Total item quantity on a delivery must not exceed vehicle capacity.
4. **Completion Rule**: Completing or failing a delivery immediately releases the driver and vehicle status to `AVAILABLE`.

---

## 6. Route Planning & Optimization

- Basic route management (`DeliveryRoute` and `DeliveryRouteStop`).
- Allows manual stop reordering (`[Move Up]`, `[Move Down]`).
- Pluggable `RouteOptimizationService` supports simple nearest-neighbor sorting if coordinates are available, preserving scheduled appointment time windows.

---

## 7. Customer Portal & Notification Integration

- **Customer View**: Customers see delivery date, scheduled window (`10:00 AM - 12:00 PM`), current status (`OUT FOR DELIVERY`), address, and item list.
- **Sanitization**: Internal notes, driver personal details, route sequences, and warehouse ops are hidden from customers.
- **Notifications**: Automated messages sent for `DELIVERY_SCHEDULED`, `DELIVERY_ASSIGNED`, `DELIVERY_STARTED`, `DELIVERY_ARRIVED`, `DELIVERY_COMPLETED`, and `DELIVERY_FAILED`.

---

## 8. REST APIs

- `POST /api/deliveries/from-warehouse-order/{warehouseOrderId}`
- `GET /api/deliveries`
- `GET /api/deliveries/{id}`
- `PATCH /api/deliveries/{id}/schedule`
- `PATCH /api/deliveries/{id}/driver`
- `PATCH /api/deliveries/{id}/vehicle`
- `POST /api/deliveries/{id}/start`
- `POST /api/deliveries/{id}/arrive`
- `POST /api/deliveries/{id}/start-setup`
- `POST /api/deliveries/{id}/complete`
- `POST /api/deliveries/{id}/fail`
- `GET /api/deliveries/dashboard`
- `GET /api/deliveries/calendar`
- `GET /api/drivers`
- `GET /api/vehicles`
- `POST /api/delivery-routes`
- `GET /api/delivery-routes`
- `GET /api/delivery-routes/{id}`
- `PATCH /api/delivery-routes/{id}/sequence`

---

## 9. RBAC & Security Matrix

| Role | Access Level |
| :--- | :--- |
| **OWNER / ADMIN** | Full access to all endpoints. |
| **OPERATIONS_MANAGER** | Create routes, schedule deliveries, assign drivers & vehicles, update status. |
| **DRIVER** | View assigned deliveries, start delivery, mark arrived, complete setup/delivery, report failure on driver dashboard. |
| **SALES / WAREHOUSE_MANAGER** | Read-only delivery visibility and readiness check. |
| **CUSTOMER** | Read-only access to own delivery tracking view. |
