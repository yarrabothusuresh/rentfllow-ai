# Day 21 — Rental Calendar, Advanced Availability & Resource Scheduling

## 1. Objective
Day 21 completes the core operational scheduling engine for RentFlow AI SaaS. It introduces:
1. A **Centralized Availability Service** as the single backend source of truth for rental inventory calculations.
2. A **Conflict Detection Service** providing multi-resource conflict checking (Inventory, Dates, Driver, Vehicle, Warehouse capacity, Turnaround buffer).
3. An **Operational Rental Calendar** (`/calendar`) supporting Month, Week, Day, and Agenda views with dynamic event generation across Bookings, Deliveries, Pickups, Returns, Maintenance, Warehouse Tasks, Driver Assignments, and Vehicle Assignments.
4. **Dedicated Resource Calendars**: Driver Calendar (`/calendar/drivers`), Vehicle Calendar (`/calendar/vehicles`), Warehouse Calendar (`/calendar/warehouse`), and Inventory Availability Calendar & Matrix (`/calendar/inventory`).
5. **Real-time Conflict Checking & Validation APIs** for Booking Creation/Editing (`POST /api/bookings/check-conflicts`), Quote requests, and Cart validation (`POST /api/availability/bulk`).
6. **Rescheduling Engine** (`POST /api/calendar/reschedule`) with conflict validation, audit logging, and automated notifications to customers, drivers, and warehouse personnel via `NotificationService`.
7. **Operational Conflict Dashboard** (`/calendar/conflicts`) for viewing and resolving conflicts or logging audit reasons for warning overrides (`SCHEDULING_WARNING_OVERRIDDEN`).
8. **Customer Availability & Alternative Suggestions** (`findAlternativeDates` & `findAlternativeProducts`) on the public storefront and customer portal without exposing internal raw stock totals.

---

## 2. Existing Architecture Findings & Integration
- **Product & Inventory State**: `Product` (`com.rentflow.ai.model.Product`) manages `quantityOwned`, `quantityInMaintenance`, `quantityDamaged`, `quantityLost`, and now `defaultTurnaroundMinutes`.
- **Reservations & Bookings**: `InventoryReservation` (`com.rentflow.ai.model.InventoryReservation`) tracks active date-based holds with statuses (`RESERVED`, `CONFIRMED`, `PENDING`, `HOLD`).
- **Warehouse Model**: `WarehouseOrder` and `WarehouseOrderItem` track warehouse workflows (PICK, PACK, CHECK_IN, INSPECTION). A `Warehouse` entity tracks daily capacities (`dailyPickCapacity`, `dailyPackCapacity`, `dailyCheckinCapacity`).
- **Delivery & Fleet Model**: `Delivery` (`com.rentflow.delivery.model.Delivery`) tracks `scheduledDate`, `scheduledStartTime`, `scheduledEndTime`, `driverId`, `vehicleId`, and status. `Driver` and `Vehicle` track resource status.
- **Maintenance & Damage**: `RepairOrder` and `DamageClaim` track items currently out of service.
- **Notification Engine**: `NotificationService` dispatches notifications for rescheduling and conflict resolution events.
- **Tenant Isolation & Security**: All services enforce strict `tenantId` boundaries and RBAC permissions (`CUSTOMER`, `DRIVER`, `WAREHOUSE_OPERATOR`, `WAREHOUSE_MANAGER`, `SALES`, `OPERATIONS_MANAGER`, `ADMIN`, `OWNER`).

---

## 3. Central Availability Engine & Calculation Algorithm
All authoritative calculations happen on the backend inside `AvailabilityService`.

$$\text{Available Quantity} = \max\left(0, \text{Quantity Owned} - \text{In Maintenance} - \text{Damaged} - \text{Lost} - \sum \text{Overlapping Reservations}\right)$$

### Date Range & Turnaround Buffer Calculation:
For a requested date window $[T_{\text{start}}, T_{\text{end}}]$, an existing reservation $[R_{\text{start}}, R_{\text{end}}]$ with product turnaround buffer $\Delta t$ conflicts if:
$$R_{\text{start}} < (T_{\text{end}} + \Delta t) \quad \text{AND} \quad (R_{\text{end}} + \Delta t) > T_{\text{start}}$$

---

## 4. Conflict Detection Architecture & Severities
`ConflictDetectionService` checks booking, inventory, driver, vehicle, and warehouse constraints.

### Conflict Severities:
1. **`HARD_CONFLICT`**: Hard block. Prevents saving/creation.
   - Inventory shortage for requested dates.
   - Driver double-booking during overlapping time window.
   - Vehicle double-booking during overlapping time window.
   - Turnaround buffer violation between consecutive rentals.
2. **`WARNING`**: Operational alert. Can be saved with explicit manager override and audit reason.
   - Warehouse daily pick/pack/check-in capacity exceeded.
   - Low inventory threshold buffer reached.
3. **`INFO`**: Informational notification.
   - Alternative dates or alternative products available.

---

## 5. Domain & Calendar Event Read Model
Calendar events are generated dynamically from domain entities without duplicating state into a separate table:
- **`BOOKING`**: Derived from `Booking` entity.
- **`DELIVERY` & `PICKUP`**: Derived from `Delivery` entity.
- **`RETURN`**: Derived from `ReturnOrder` entity.
- **`MAINTENANCE`**: Derived from `RepairOrder` / `Inspection` entity.
- **`WAREHOUSE_TASK`**: Derived from `WarehouseOrder` entity.
- **`DRIVER_ASSIGNMENT`**: Active `Delivery` linked to a `Driver`.
- **`VEHICLE_ASSIGNMENT`**: Active `Delivery` linked to a `Vehicle`.
- **`EVENT`**: Derived from `Event` entity.

---

## 6. Calendar & Scheduling APIs

### Calendar APIs (`/api/calendar/...`)
- `GET /api/calendar`: Multi-type, multi-resource operational calendar events query (`start`, `end`, `eventTypes`, `resourceType`, `resourceId`, `status`, `warehouseId`).
- `POST /api/calendar/reschedule`: Reschedules a delivery, pickup, or booking event with conflict check and notification.
- `GET /api/calendar/dashboard`: Daily operational summary metrics (bookings, deliveries, pickups, returns, maintenance, conflicts, warnings).
- `GET /api/calendar/conflicts`: Operational conflict log (`OPEN`, `ACKNOWLEDGED`, `RESOLVED`, `OVERRIDDEN`).
- `POST /api/calendar/conflicts/{id}/override`: Override warning conflict with mandatory reason string.

### Availability APIs (`/api/availability/...`)
- `GET /api/availability`: Check single product date-range availability.
- `POST /api/availability/bulk`: Bulk availability check for cart/quotes.
- `GET /api/availability/matrix`: Date-range matrix across catalog products.
- `GET /api/availability/alternatives`: Find alternative date ranges or similar products.
- `POST /api/bookings/check-conflicts`: Validate draft booking before saving (excludes `currentBookingId`).

---

## 7. Role-Based Access Control (RBAC) Matrix
| Role | View Operational Calendar | Reschedule Events | Override Warnings | Driver Calendar | Vehicle Calendar | Warehouse Calendar | Customer View |
| :--- | :---: | :---: | :---: | :---: | :---: | :---: | :---: |
| **OWNER / ADMIN** | Full Access | ✅ | ✅ | Full | Full | Full | All |
| **OPERATIONS_MANAGER** | Full Access | ✅ | ✅ | Full | Full | Full | All |
| **WAREHOUSE_MANAGER** | Operational | Task Only | ❌ | View | View | Full | ❌ |
| **WAREHOUSE_OPERATOR** | Tasks Only | ❌ | ❌ | ❌ | ❌ | Tasks Only | ❌ |
| **DRIVER** | Own Schedule | Status Only | ❌ | Own Only | Assigned Only | ❌ | ❌ |
| **SALES** | Bookings/Quotes | ❌ | ❌ | View | View | View | All |
| **CUSTOMER** | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ | Own Schedule Only |

---

## 8. Timezone & Daylight Saving Time Strategy
- Standardized handling using Java `ZoneId`, `ZonedDateTime`, and `Instant`.
- Tenant timezone (e.g. `America/New_York`) set via tenant settings and applied at calendar presentation boundaries.
- All stored database timestamps use UTC / ISO-8601 format.

---

## 9. Verification & Demo Scenarios
1. **Inventory Hard Conflict**: 500 total chairs, 400 reserved, requesting 200 -> `HARD_CONFLICT` ("Only 100 available").
2. **Driver Double-Booking**: Driver assigned 10:00-12:00, new delivery 11:00-13:00 -> `HARD_CONFLICT` ("John Smith is already assigned").
3. **Vehicle Double-Booking**: Vehicle assigned 10:00-12:00, new delivery 11:00-13:00 -> `HARD_CONFLICT` ("VAN-01 is already assigned").
4. **Turnaround Buffer**: Rental ends Sep 12 5:00 PM (60 min turnaround). Request start Sep 12 5:30 PM -> `HARD_CONFLICT`. Request start Sep 12 6:30 PM -> `AVAILABLE`.
5. **Warehouse Capacity Warning**: Warehouse capacity 20 orders/day, 19 scheduled, adding 5 -> `WARNING` ("Warehouse capacity exceeded by 4 orders"). Allows manager override with logged reason `SCHEDULING_WARNING_OVERRIDDEN`.
6. **Booking Edit Exclusion**: Editing booking `BOOK-000123` excludes its own reservation from conflict checking.

---

## 10. Audit Events Triggered
- `BOOKING_CONFLICT_DETECTED`
- `INVENTORY_CONFLICT_DETECTED`
- `DRIVER_CONFLICT_DETECTED`
- `VEHICLE_CONFLICT_DETECTED`
- `WAREHOUSE_CAPACITY_WARNING`
- `SCHEDULING_WARNING_OVERRIDDEN`
- `BOOKING_RESCHEDULED`
- `DELIVERY_RESCHEDULED`
- `PICKUP_RESCHEDULED`
- `CALENDAR_EVENT_UPDATED`
