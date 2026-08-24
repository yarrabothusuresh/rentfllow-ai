# Day 15 — Warehouse Operations Module

## 1. Objective
Build the **Warehouse Operations Module** to enable rental companies to prepare booked rental equipment for delivery.

Core workflow:
`BOOKING` → `INVENTORY RESERVED` → `WAREHOUSE WORK ORDER` → `PICK ITEMS` → `PACK ITEMS` → `READY FOR DELIVERY`

The warehouse team gains full real-time visibility over:
- Event & booking details (event name, event date, venue, customer)
- Required vs. picked vs. packed item quantities
- Item inventory storage locations (e.g. A-01-03)
- Shortages and operational exceptions with manager confirmation
- Work order priority and staff assignments

---

## 2. Existing Inventory Architecture
RentFlow AI manages inventory availability through:
1. **Catalog Products (`Product`)**: Maintains total physical stock (`quantityOwned`), maintenance, damaged, and lost quantities.
2. **Inventory Reservations (`InventoryReservation`)**: Reserves equipment quantities for confirmed bookings over start and end datetime windows.
3. **Inventory Transactions (`InventoryTransaction`)**: Immutable audit ledger recording stock purchases, reservations, releases, and adjustments.

### Warehouse Module Integration Rule
- Warehouse picking operates directly on valid `InventoryReservation` instances.
- Picking does **not** duplicate inventory availability logic or silently alter physical stock counts.
- Picking transitions operational tracking (`RESERVED` → `PICKED`).

---

## 3. Warehouse Work Order Lifecycle & Statuses

```
              ┌────────────────┐
              │    BOOKING     │
              └───────┬────────┘
                      │ (Confirmation & Inventory Reserved)
                      ▼
              ┌────────────────┐
              │    PENDING     │
              └───────┬────────┘
                      │ (Order Initialized)
                      ▼
              ┌────────────────┐
              │ READY_TO_PICK  │
              └───────┬────────┘
                      │ (Staff Starts Picking)
                      ▼
              ┌────────────────┐
              │    PICKING     │
              └───────┬────────┘
                      │ (All items picked or shortage confirmed)
                      ▼
              ┌────────────────┐
              │     PICKED     │
              └───────┬────────┘
                      │ (Staff Starts Packing)
                      ▼
              ┌────────────────┐
              │    PACKING     │
              └───────┬────────┘
                      │ (All items packed)
                      ▼
              ┌────────────────┐
              │     PACKED     │
              └───────┬────────┘
                      │ (Staged for Truck Loading)
                      ▼
          ┌────────────────────────┐
          │   READY_FOR_DELIVERY   │
          └────────────────────────┘
```

### Work Order Statuses (`WarehouseOrderStatus`)
- `PENDING`: Initial state upon creation.
- `READY_TO_PICK`: Staged for warehouse team to start picking.
- `PICKING`: Active picking in progress by assigned worker.
- `PICKED`: All required items picked (or shortage manager-confirmed).
- `PACKING`: Active packing and staging in progress.
- `PACKED`: All items packed into bins/crates/truck staging.
- `READY_FOR_DELIVERY`: Staging complete, ready for driver loading.
- `CANCELLED`: Work order cancelled (e.g., booking cancelled).

### Work Order Item Statuses (`WarehouseOrderItemStatus`)
- `PENDING`: Not yet picked.
- `PICKED`: Fully picked (`quantityPicked == quantityRequired`).
- `PARTIALLY_PICKED`: Partially picked (`0 < quantityPicked < quantityRequired`).
- `PACKED`: Fully packed (`quantityPacked == quantityRequired`).
- `SHORT`: Unresolved item shortage reported.
- `DAMAGED`: Damaged item identified during pick.

---

## 4. Database Schema

### `warehouse_orders`
- `id` (UUID, Primary Key)
- `tenant_id` (VARCHAR, Indexed)
- `booking_id` (UUID, Indexed)
- `event_id` (UUID, Indexed)
- `customer_id` (UUID, Indexed)
- `order_number` (VARCHAR, Unique, format: `WH-000001`)
- `scheduled_date` (TIMESTAMP)
- `priority` (VARCHAR: `LOW`, `NORMAL`, `HIGH`, `URGENT`)
- `status` (VARCHAR: `PENDING`, `READY_TO_PICK`, `PICKING`, `PICKED`, `PACKING`, `PACKED`, `READY_FOR_DELIVERY`, `CANCELLED`)
- `assigned_to` (VARCHAR)
- `notes` (TEXT)
- `created_by` (VARCHAR)
- `created_at` (TIMESTAMP)
- `updated_at` (TIMESTAMP)
- `completed_at` (TIMESTAMP)

### `warehouse_order_items`
- `id` (UUID, Primary Key)
- `warehouse_order_id` (UUID, Foreign Key)
- `booking_item_id` (UUID)
- `product_id` (UUID)
- `product_name_snapshot` (VARCHAR)
- `sku_snapshot` (VARCHAR)
- `location_snapshot` (VARCHAR: e.g. `A-01-03`)
- `quantity_required` (INT)
- `quantity_picked` (INT)
- `quantity_packed` (INT)
- `status` (VARCHAR)
- `notes` (TEXT)
- `created_at` (TIMESTAMP)
- `updated_at` (TIMESTAMP)

### `warehouse_locations`
- `id` (UUID, Primary Key)
- `tenant_id` (VARCHAR, Indexed)
- `code` (VARCHAR, e.g. `A-01-03`)
- `name` (VARCHAR)
- `description` (TEXT)
- `active` (BOOLEAN)
- `created_at` (TIMESTAMP)
- `updated_at` (TIMESTAMP)

### `warehouse_audits`
- `id` (UUID, Primary Key)
- `tenant_id` (VARCHAR, Indexed)
- `warehouse_order_id` (UUID)
- `booking_id` (UUID)
- `action` (VARCHAR)
- `performed_by` (VARCHAR)
- `details` (TEXT)
- `timestamp` (TIMESTAMP)

---

## 5. Picking & Packing Workflows

### Picking Workflow
1. Operator selects **[Start Picking]** → Work order transitions `READY_TO_PICK` → `PICKING`.
2. Operator updates picked quantity for an item → `POST /api/warehouse/orders/{id}/items/{itemId}/pick`.
3. If quantity picked equals quantity required, item status becomes `PICKED`.
4. If quantity picked is less than required, item status becomes `PARTIALLY_PICKED`.
5. If inventory is missing/damaged, operator enters shortage notes → `WAREHOUSE_ITEM_SHORT` notification emitted to Operations Manager.
6. Progress is computed as `(totalQuantityPicked / totalQuantityRequired) * 100`.
7. Operator clicks **[Complete Picking]** → If shortages exist, system requires explicit manager confirmation (`confirmShortage = true`). Upon confirmation, order transitions to `PICKED`.

### Packing Workflow
1. Operator selects **[Start Packing]** → Work order transitions `PICKED` → `PACKING`.
2. Operator updates packed quantity → `POST /api/warehouse/orders/{id}/items/{itemId}/pack`. Quantity packed cannot exceed quantity picked.
3. Once all items are packed, operator clicks **[Complete Packing]** → Work order transitions to `PACKED` and then `READY_FOR_DELIVERY`.
4. `WAREHOUSE_READY_FOR_DELIVERY` event triggers internal notification to operations/delivery teams.

---

## 6. REST APIs

- `POST /api/warehouse/orders/from-booking/{bookingId}` — Generate warehouse order from booking.
- `GET /api/warehouse/orders` — List warehouse orders with search, status/priority filtering, and pagination.
- `GET /api/warehouse/orders/{id}` — Retrieve detailed warehouse order with item list and progress.
- `POST /api/warehouse/orders/{id}/start-picking` — Begin picking workflow.
- `POST /api/warehouse/orders/{id}/items/{itemId}/pick` — Pick specified quantity of an item.
- `POST /api/warehouse/orders/{id}/complete-picking` — Complete picking phase.
- `POST /api/warehouse/orders/{id}/start-packing` — Begin packing workflow.
- `POST /api/warehouse/orders/{id}/items/{itemId}/pack` — Pack specified quantity of an item.
- `POST /api/warehouse/orders/{id}/complete-packing` — Complete packing phase and mark ready for delivery.
- `PATCH /api/warehouse/orders/{id}/assign` — Assign work order to warehouse staff member.
- `GET /api/warehouse/dashboard` — Operational metrics dashboard (today orders, ready to pick, shortages, urgent priorities).
- `GET /api/warehouse/locations` — List active warehouse storage locations.

---

## 7. Angular Screens & Navigation

- `/warehouse/dashboard` — High-level warehouse operational metrics and daily priorities.
- `/warehouse/orders` — Filterable work order list with status badges and quick actions.
- `/warehouse/orders/:id` — Detail view featuring tablet-responsive touch controls (`+`, `-`, `[Mark Picked]`, `[Report Shortage]`), progress bars, and packing checklist.
- `/warehouse/pick` — Focused pick list view for active picking operations.
- `/warehouse/packing` — Packing checklist view.
- `/warehouse/shortages` — Shortage exception management dashboard.

---

## 8. RBAC & Multi-Tenant Security

- `OWNER` / `ADMIN`: Full creation, management, picking, packing, and assignment access.
- `WAREHOUSE_MANAGER`: Full operational access (create, assign, pick, pack, confirm shortages).
- `WAREHOUSE_OPERATOR`: Access to assigned/available orders, pick items, pack items.
- `SALES` / `FINANCE`: Read-only access to operational statuses.
- `CUSTOMER`: Zero internal warehouse access (403 Forbidden). Customer portal presents aggregated `PREPARING` or `READY_FOR_DELIVERY` status.
- **Tenant Isolation**: Every JPA query filters by `tenantId`. Cross-tenant work order access returns `404/403`.

---

## 9. Notification & Audit Integration

### Notifications
Integrates with Day 14 `NotificationService`:
- `WAREHOUSE_ORDER_CREATED`: Notifies warehouse team of new work orders.
- `WAREHOUSE_ITEM_SHORT`: Notifies Operations Manager of item shortages.
- `WAREHOUSE_ORDER_READY`: Notifies operations and delivery teams that order is ready for dispatch.

### Audit Events
- `WAREHOUSE_ORDER_CREATED`
- `WAREHOUSE_ORDER_ASSIGNED`
- `WAREHOUSE_PICKING_STARTED`
- `WAREHOUSE_ITEM_PICKED`
- `WAREHOUSE_ITEM_SHORT`
- `WAREHOUSE_PICKING_COMPLETED`
- `WAREHOUSE_PACKING_STARTED`
- `WAREHOUSE_ITEM_PACKED`
- `WAREHOUSE_PACKING_COMPLETED`
- `WAREHOUSE_READY_FOR_DELIVERY`

---

## 10. Demo Data & Verification Flow

### Demo Data Setup
- **Main Warehouse Locations**: `A-01-01`, `A-01-02`, `A-01-03`, `B-01-01`, `B-02-01`, `C-01-01`.
- **Demo Booking `BOOK-000123`**:
  - Customer: ABC Events LLC
  - Event: Wedding Reception (Aug 30, 2026)
  - Items: 100 Chiavari Chairs, 10 Round Tables, 20 White Linens.
- **Demo Warehouse Order `WH-000123`**: Staged with `HIGH` priority.

### Shortage Demo Flow
- Second work order staged requiring 100 chairs with 95 picked and 5 reported short. Demonstrates manager shortage confirmation override flow.

---

## 11. Future Architectural Extensions (Days 16+)

- **`InventoryIdentificationService` Interface**: Stubbed service abstraction for future barcode and QR code scanning integrations (SKU scan, barcode scan, location verification).
- **AI Warehouse Recommendations**: Prepared data structures for AI work order risk assessment, daily picking prioritization, and event volume analysis without autonomous state mutation.
