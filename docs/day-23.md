# DAY 23 — WAREHOUSE OPERATIONS 2.0
## PICK / PACK / LOAD, KITTING, CHECKLISTS, MOBILE WORKFLOWS & EXCEPTIONS

---

## 1. OBJECTIVE

Upgrade the RentFlow AI warehouse module into a comprehensive, high-reliability rental fulfillment pipeline:

$$\text{BOOKING} \longrightarrow \text{RESERVATION} \longrightarrow \text{WAREHOUSE ORDER} \longrightarrow \text{PICK LIST} \longrightarrow \text{PICK} \longrightarrow \text{VERIFY} \longrightarrow \text{PACK} \longrightarrow \text{KIT CHECK} \longrightarrow \text{LOAD} \longrightarrow \text{VERIFY LOAD} \longrightarrow \text{DRIVER HANDOFF} \longrightarrow \text{DELIVERY}$$

This system guarantees:
1. Ordered traversal pick lists grouped by warehouse zone/aisle/rack/shelf.
2. Dual support for serialized asset scan-picking and bulk quantity picking.
3. Strict exception handling for shortages, damaged items, and missing kit components with blocking guards.
4. Formal manager-reviewed substitution flows.
5. Container and case packing (`CASE`, `CART`, `PALLET`, `BAG`).
6. Kit/bundle definition breakdown and completeness verification.
7. Dispatch load lists with vehicle capacity checks.
8. Driver receipt verification before delivery routes can be started.
9. Mobile/tablet-first touch interfaces with single-item focus and barcode scan integration.
10. Complete tenant isolation, RBAC, concurrency safety, and audit logging.

---

## 2. EXISTING ARCHITECTURE FINDINGS & REUSE PLAN

### Existing Architecture Inspected:
* **`WarehouseOrder` & `WarehouseOrderItem`**: Previously introduced in Day 15 for baseline order creation. Statuses: `PENDING`, `READY_TO_PICK`, `PICKING`, `PICKED`, `PACKING`, `PACKED`, `READY_FOR_DELIVERY`, `CANCELLED`.
* **`InventoryItem` (Day 22)**: Manages physical serialized assets with `assetCode`, `serialNumber`, `barcode`, `qrCode`, `status` (`AVAILABLE`, `RESERVED`, `PICKED`, `OUT_ON_RENT`, `DAMAGED`, `INSPECTION`, etc.), `condition`, and `currentLocation`.
* **`StockMovement` (Day 22)**: Authoritative immutable inventory log supporting movement types `RESERVATION`, `RELEASE`, `CHECKOUT`, `RETURN`, `DAMAGE`, `ADJUSTMENT_IN/OUT`.
* **`InventoryReservation`**: Booking-level reserved stock guaranteeing item availability for confirmed events.
* **`Product` & `ProductType`**: Includes `RENTAL_ITEM`, `PACKAGE`, `CONSUMABLE`, `SERVICE`.
* **`Delivery` (Day 17)**: Links `bookingId`, `warehouseOrderId`, `driverId`, `vehicleId`, and delivery execution lifecycle.
* **`NotificationService`**: Publishes in-app and email/SMS alerts to tenants and users.

### What Is Enhanced in Day 23:
1. **Warehouse Order Lifecycle**: Extended with `VERIFYING`, `LOADING`, `LOADED`, `HANDED_TO_DRIVER`, `SHORT`, `BLOCKED`.
2. **Pick List Domain**: Structured `PickList` and `PickListItem` entities with location sequencing (`A-01-01` -> `A-01-02` -> `B-01-01`).
3. **Serialized Asset Scanning**: Barcode/QR validation verifying correct tenant, warehouse, product match, non-duplicate pick, and transition from `RESERVED` -> `PICKED`.
4. **Exceptions & Substitutions**: Dedicated `WarehouseException` entity with severity levels (`INFO`, `WARNING`, `BLOCKING`) and `WarehouseSubstitution` model for formal review.
5. **Pack List & Containers**: `PackList`, `PackListItem`, and `PackingContainer` entities enabling containerized asset assignment.
6. **Kit & Bundle Breakdown**: `KitDefinition` and `KitComponent` models ensuring kits (such as Lighting Kits or Wedding Table Sets) are verified for all sub-components.
7. **Load List & Driver Handoff**: `LoadList`, `LoadListItem`, vehicle capacity calculation, and driver handoff sign-off.
8. **Delivery Guard**: Enforced precondition in `DeliveryService.startDelivery` requiring warehouse order `HANDED_TO_DRIVER`.
9. **Operational Checklists**: `WarehouseOrderChecklist` for pick/pack/load quality assurance.
10. **Mobile Interface**: Touch-optimized `/warehouse/pick-lists/:id/mobile` view.

---

## 3. WAREHOUSE ORDER LIFECYCLE

```
[ PENDING ]
     │
     ▼
[ READY_TO_PICK ]
     │
     ▼
[ PICKING ] ──( Shortage / Damaged / Missing )──► [ SHORT / BLOCKED ]
     │                                                   │
     ▼                                            (Resolution / Approval)
[ PICKED ] ◄─────────────────────────────────────────────┘
     │
     ▼
[ VERIFYING ]
     │
     ▼
[ PACKING ]
     │
     ▼
[ PACKED ]
     │
     ▼
[ LOADING ]
     │
     ▼
[ LOADED ]
     │
     ▼
[ READY_FOR_DELIVERY ]
     │
     ▼
[ HANDED_TO_DRIVER ]
     │
     ▼
( Delivery Workflow Starts )
```

---

## 4. DOMAIN MODELS & ENTITIES

### 4.1 PickList & PickListItem
* **`PickList`**:
  * `id`: UUID (PK)
  * `tenantId`: String (Indexed)
  * `pickListNumber`: String (`PICK-000001`)
  * `warehouseOrderId`: UUID
  * `warehouseId`: UUID
  * `status`: `PENDING`, `IN_PROGRESS`, `PARTIAL`, `COMPLETED`, `BLOCKED`, `CANCELLED`
  * `priority`: `URGENT`, `HIGH`, `NORMAL`, `LOW`
  * `assignedTo`: String
  * `startedAt`, `completedAt`, `createdAt`, `updatedAt`

* **`PickListItem`**:
  * `id`: UUID (PK)
  * `tenantId`: String
  * `pickListId`: UUID
  * `bookingItemId`: UUID
  * `productId`: UUID
  * `inventoryItemId`: UUID (optional, for pre-assigned serialized asset)
  * `warehouseLocationId`: UUID / `locationCode`
  * `requiredQuantity`: int
  * `pickedQuantity`: int
  * `shortQuantity`: int
  * `damagedQuantity`: int
  * `substitutedQuantity`: int
  * `status`: `PENDING`, `PICKING`, `PARTIAL`, `PICKED`, `SHORT`, `DAMAGED`, `SUBSTITUTED`, `SKIPPED`
  * `sequenceNumber`: int

### 4.2 PackingContainer & PackList
* **`PackingContainer`**:
  * `id`: UUID
  * `tenantId`: String
  * `containerCode`: String (`CASE-001`, `CART-002`, `BAG-001`, `PALLET-003`)
  * `type`: `CASE`, `CART`, `PALLET`, `BAG`, `OTHER`
  * `status`: `AVAILABLE`, `IN_USE`, `PACKED`, `LOADED`
  * `warehouseId`: UUID

* **`PackList` & `PackListItem`**:
  * `packListNumber`: String (`PACK-000001`)
  * `warehouseOrderId`: UUID
  * `status`: `PENDING`, `IN_PROGRESS`, `PARTIAL`, `COMPLETED`, `BLOCKED`
  * `containerId`: UUID (links items to physical containers)

### 4.3 KitDefinition & KitComponent
* **`KitDefinition`**:
  * `id`: UUID
  * `tenantId`: String
  * `productId`: UUID (the composite product/package)
  * `name`: String (e.g., "Lighting Kit", "Wedding Table Kit")

* **`KitComponent`**:
  * `kitDefinitionId`: UUID
  * `componentProductId`: UUID
  * `quantityPerKit`: int (e.g. 4 uplights, 1 controller, 4 cables)

### 4.4 LoadList & Driver Handoff
* **`LoadList` & `LoadListItem`**:
  * `loadListNumber`: String (`LOAD-000001`)
  * `warehouseOrderId`: UUID
  * `deliveryId`: UUID
  * `vehicleId`: UUID
  * `driverId`: UUID
  * `status`: `PENDING`, `LOADING`, `LOADED`, `VERIFIED`, `HANDED_OFF`, `BLOCKED`

### 4.5 WarehouseException & WarehouseSubstitution
* **`WarehouseException`**:
  * `type`: `SHORTAGE`, `DAMAGE`, `WRONG_LOCATION`, `MISSING_ASSET`, `KIT_INCOMPLETE`, `PACKING_ISSUE`, `LOAD_ISSUE`, `VEHICLE_CAPACITY`, `OTHER`
  * `severity`: `INFO`, `WARNING`, `BLOCKING`
  * `status`: `OPEN`, `ACKNOWLEDGED`, `RESOLVED`, `WAIVED`, `CANCELLED`
  * `resolution`: `FOUND_ITEM`, `SUBSTITUTION`, `QUANTITY_REDUCED`, `REPAIRED`, `REPACKED`, `VEHICLE_CHANGED`, `MANAGER_OVERRIDE`, `OTHER`

* **`WarehouseSubstitution`**:
  * `warehouseOrderId`: UUID
  * `originalProductId`: UUID
  * `replacementProductId`: UUID
  * `originalQuantity`: int
  * `replacementQuantity`: int
  * `status`: `PROPOSED`, `APPROVED`, `REJECTED`, `APPLIED`
  * `reason`: String
  * `approvedBy`: String

---

## 5. REST APIS

| Method | Path | Description | Roles |
|---|---|---|---|
| `POST` | `/api/warehouse/orders/{orderId}/pick-list` | Generate sequential pick list from warehouse order | `WAREHOUSE_OPERATOR+` |
| `GET` | `/api/warehouse/pick-lists` | List pick lists with filters | `WAREHOUSE_OPERATOR+` |
| `GET` | `/api/warehouse/pick-lists/{id}` | Get pick list details and items | `WAREHOUSE_OPERATOR+` |
| `POST` | `/api/warehouse/pick-lists/{id}/start` | Start pick list progress | `WAREHOUSE_OPERATOR+` |
| `POST` | `/api/warehouse/pick-lists/{id}/scan` | Barcode/QR scan pick validation | `WAREHOUSE_OPERATOR+` |
| `POST` | `/api/warehouse/pick-lists/{id}/items/{itemId}/pick` | Quantity or manual item pick | `WAREHOUSE_OPERATOR+` |
| `POST` | `/api/warehouse/pick-lists/{id}/complete` | Complete pick list (blocked if unresolved exceptions) | `WAREHOUSE_OPERATOR+` |
| `POST` | `/api/warehouse/pick-lists/{id}/verify` | Perform pick verification | `WAREHOUSE_MANAGER+` |
| `GET` | `/api/warehouse/pack-lists` | List pack lists | `WAREHOUSE_OPERATOR+` |
| `GET` | `/api/warehouse/pack-lists/{id}` | Get pack list with containers and kit checklists | `WAREHOUSE_OPERATOR+` |
| `POST` | `/api/warehouse/pack-lists/{id}/start` | Start packing | `WAREHOUSE_OPERATOR+` |
| `POST` | `/api/warehouse/pack-lists/{id}/items/{itemId}/pack` | Pack items into containers | `WAREHOUSE_OPERATOR+` |
| `POST` | `/api/warehouse/pack-lists/{id}/complete` | Complete packing | `WAREHOUSE_OPERATOR+` |
| `GET` | `/api/warehouse/load-lists` | List load lists | `WAREHOUSE_OPERATOR+` |
| `GET` | `/api/warehouse/load-lists/{id}` | Get load list and vehicle status | `WAREHOUSE_OPERATOR+` |
| `POST` | `/api/warehouse/load-lists/{id}/start` | Start loading | `WAREHOUSE_OPERATOR+` |
| `POST` | `/api/warehouse/load-lists/{id}/items/{itemId}/load` | Mark container or item loaded | `WAREHOUSE_OPERATOR+` |
| `POST` | `/api/warehouse/load-lists/{id}/verify` | Verify all items loaded | `WAREHOUSE_MANAGER+` |
| `POST` | `/api/warehouse/load-lists/{id}/handoff` | Driver confirms load handoff | `DRIVER`, `WAREHOUSE_MANAGER+` |
| `GET` | `/api/warehouse/exceptions` | List exceptions | `WAREHOUSE_OPERATOR+` |
| `POST` | `/api/warehouse/exceptions` | Report new shortage or damage exception | `WAREHOUSE_OPERATOR+` |
| `POST` | `/api/warehouse/exceptions/{id}/resolve` | Resolve exception with reason | `OPERATIONS_MANAGER`, `ADMIN`, `OWNER` |
| `GET` | `/api/warehouse/substitutions` | List proposed substitutions | `WAREHOUSE_OPERATOR+` |
| `POST` | `/api/warehouse/substitutions` | Propose item substitution | `WAREHOUSE_OPERATOR+` |
| `POST` | `/api/warehouse/substitutions/{id}/approve` | Approve substitution | `OPERATIONS_MANAGER`, `ADMIN`, `OWNER` |
| `POST` | `/api/warehouse/substitutions/{id}/reject` | Reject substitution | `OPERATIONS_MANAGER`, `ADMIN`, `OWNER` |
| `GET` | `/api/warehouse/containers` | List registered containers | `WAREHOUSE_OPERATOR+` |
| `POST` | `/api/warehouse/containers` | Create new container | `WAREHOUSE_MANAGER+` |
| `GET` | `/api/warehouse/my-work` | Operator's active assignments | `WAREHOUSE_OPERATOR+` |
| `GET` | `/api/warehouse/metrics` | Productivity and speed metrics | `WAREHOUSE_MANAGER+` |

---

## 6. ANGULAR USER INTERFACES

1. **Warehouse Dashboard 2.0 (`/warehouse/dashboard`)**:
   * Stage funnel: Ready to Pick -> Picking -> Verification -> Packing -> Loading -> Ready for Delivery -> Handed Off.
   * Productivity cards (Today's picked orders, pick duration, pack duration, exception rate).
   * Urgent dispatch queue and shortcut actions.

2. **Operator My Work (`/warehouse/my-work`)**:
   * Personalized, priority-sorted tasks (Pick Lists, Pack Lists, Load Lists, Exceptions).

3. **Pick Lists & Mobile Pick Mode (`/warehouse/pick-lists`, `/warehouse/pick-lists/:id/mobile`)**:
   * Desktop view with complete sequence list.
   * Mobile touch view displaying one item at a time with big buttons (+1, +5, Enter Qty, Scan Barcode, Shortage, Damaged, Next).

4. **Pack Lists & Containers (`/warehouse/pack-lists/:id`, `/warehouse/containers`)**:
   * Container selector (`BAG-001`, `CASE-001`, `CART-001`).
   * Kit component breakdown with completeness badges.

5. **Load Lists & Driver Handoff (`/warehouse/load-lists/:id`)**:
   * Vehicle capacity indicator and overweight/over-cube warnings.
   * Driver sign-off modal and immediate delivery unlocking.

6. **Exceptions & Substitutions (`/warehouse/exceptions`, `/warehouse/substitutions`)**:
   * Severity badges (BLOCKING vs WARNING vs INFO).
   * One-click resolution modal and price comparison review.

---

## 7. TENANT ISOLATION, RBAC & SAFETY

* **Tenant Isolation**: Every database query filters by `tenantId`. Cross-tenant scanning or loading attempts are rejected with `404 Not Found` or `400 Bad Request`.
* **RBAC**:
  * `WAREHOUSE_OPERATOR`: Pick, pack, load, scan, report exceptions.
  * `WAREHOUSE_MANAGER`: Assign lists, verify picks/loads, resolve exceptions.
  * `OPERATIONS_MANAGER` / `ADMIN` / `OWNER`: Approve substitutions, override capacity warnings, resolve blocking issues.
  * `DRIVER`: Confirm load receipts at handoff.
  * `CUSTOMER`: Never sees internal warehouse pick lists, container IDs, locations, or exceptions; only customer-safe statuses (`Preparing`, `Packing`, `Ready for Delivery`, `Out for Delivery`).
* **Concurrency & Idempotency**:
  * Optimistic locking (`@Version`) on `InventoryItem` prevents duplicate picks of the same serialized asset.
  * Scan requests verify `status == AVAILABLE || RESERVED` before transitioning to `PICKED`. Re-scanning an already-picked item returns `"This asset has already been picked."` without duplicate `StockMovement` entries.

---

## 8. DEMO DATA SCENARIO

* **Booking**: `BOOK-000456`
* **Customer**: ABC Events LLC
* **Event**: Wedding Reception (Tomorrow 10:00 AM)
* **Warehouse**: Main Warehouse
* **Items**:
  * 100 Chiavari Chairs (20 serialized assets `CHR-000101`..`CHR-000120` + 80 quantity)
  * 10 Round Banquet Tables (`TAB-000101`..`TAB-000110`)
  * 20 Linens (Quantity based)
  * 2 Lighting Kits (Each containing 4 Uplights, 1 Controller, 4 Cables)
* **Pre-seeded Containers**: `BAG-001`, `CASE-001`, `CART-001`, `PALLET-001`
* **Pre-seeded Vehicles**: `VAN-01` (Driver: John Smith)
