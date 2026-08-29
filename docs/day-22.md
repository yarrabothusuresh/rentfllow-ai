# DAY 22 — INVENTORY MANAGEMENT 2.0: SERIALIZED ASSETS, BARCODE/QR, STOCK MOVEMENTS & MULTI-WAREHOUSE INVENTORY

## 1. Objective & Overview
Day 22 upgrades the RentFlow AI inventory engine to support both **QUANTITY-BASED** and **SERIALIZED ASSETS** across multiple warehouses, with barcode/QR scanning, stock movement audit trail, transfer workflows, cycle counts, asset allocation, checkout/return scanning, and full integration into the Day 21 `AvailabilityService`.

---

## 2. Existing Inventory & Related Architecture Analysis

1. **Existing Inventory Model**:
   - `Product` (`products` table): Holds `quantityOwned`, `quantityInMaintenance`, `quantityDamaged`, `quantityLost`. Tracks overall product configuration, rental rate, replacement cost, and turnaround time buffer.
   - `InventoryTransaction` (`inventory_transactions` table): Records stock adjustments (`PURCHASE`, `ADJUSTMENT`, `DAMAGE`, `LOSS`, `RESTORED`).
   - `InventoryReservation` (`inventory_reservations` table): Reserves quantity of a product for a date window (`startDateTime` to `endDateTime`) tied to `eventId` / `bookingId`. Has optional `inventoryItemId`.

2. **Existing Warehouse Model**:
   - `Warehouse` (`warehouses` table): Has `id`, `tenantId`, `code`, `name`, `address`, `dailyPickCapacity`, `dailyPackCapacity`, `dailyCheckinCapacity`, `active`.
   - `WarehouseLocation` (`warehouse_locations` table): Tracks aisle/shelf bin locations per warehouse.

3. **Existing Inventory States & Asset Lifecycle**:
   - Products currently track coarse aggregated numbers (`quantityOwned`, `quantityInMaintenance`, `quantityDamaged`, `quantityLost`).
   - Day 22 introduces individual `InventoryItem` records for serialized products and `WarehouseStock` balances per warehouse for quantity-based products.

4. **Existing Reservation & Booking Relationship**:
   - `Booking` (`bookings` table) contains `BookingItem`s with product ID and quantity.
   - `InventoryReservation` locks product quantities during rental dates.
   - Day 22 adds explicit physical asset allocation (`BookingAllocation` / `InventoryItem.bookingId`).

5. **Existing Return & Maintenance Relationship**:
   - `ReturnOrder` & `Inspection`: Mark items as returned, inspected, damaged, or lost.
   - `RepairOrder` & `DamageClaim`: Handle maintenance schedules and damage claims.
   - Day 22 connects asset status transitions (`AVAILABLE` -> `OUT_ON_RENT` -> `RETURNED` -> `INSPECTION` -> `DAMAGED` / `MAINTENANCE` -> `AVAILABLE`).

6. **Existing Stock Movement Logic**:
   - `InventoryTransaction` logged basic quantity adjustments.
   - Day 22 introduces `StockMovement` as the authoritative, transactional ledger for every inventory change.

7. **What Can Be Reused**:
   - `Product`, `Warehouse`, `WarehouseLocation`, `InventoryReservation`, `Booking`, `ReturnOrder`, `DamageClaim`, `RepairOrder`.
   - `AvailabilityService` from Day 21 (authoritative engine extended to filter multi-warehouse stock and asset status).
   - Audit and Security infrastructures (`TenantContext`, RBAC permissions).

8. **What Needs To Be Added**:
   - `trackingType` enum (`QUANTITY`, `SERIALIZED`) on `Product`.
   - `InventoryItem` entity for individual serialized assets with `assetCode`, `serialNumber`, `barcode`, `qrCode`, `status`, `condition`, `warehouseId`, `bookingId`, `currentLocation`.
   - `WarehouseStock` entity for quantity-based inventory counts per warehouse.
   - `StockMovement` entity & `MovementType` enum (`RECEIPT`, `ADJUSTMENT_IN`, `ADJUSTMENT_OUT`, `TRANSFER_OUT`, `TRANSFER_IN`, `RESERVATION`, `RELEASE`, `CHECKOUT`, `RETURN`, `DAMAGE`, `MAINTENANCE`, `MAINTENANCE_RETURN`, `LOSS`, `RECOVERY`, `RETIREMENT`).
   - `InventoryTransfer` & `InventoryTransferItem` for multi-warehouse transfers.
   - `InventoryCycleCount` & `CycleCountItem` for physical inventory audits.
   - Asset Status lifecycle (`AVAILABLE`, `RESERVED`, `PICKED`, `OUT_ON_RENT`, `RETURNED`, `INSPECTION`, `DAMAGED`, `MAINTENANCE`, `LOST`, `RETIRED`, `TRANSFER_PENDING`).
   - Asset Condition (`NEW`, `EXCELLENT`, `GOOD`, `FAIR`, `DAMAGED`, `UNUSABLE`).
   - Barcode/QR scanning endpoints & lookup token logic.
   - Booking allocation & checkout scan endpoints.
   - Angular UI components for Inventory 2.0 (Dashboard, Asset List, Asset Detail, Stock Receiving, Adjustment, Transfers, Scanning, Cycle Counts).
