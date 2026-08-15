# Inventory Audit Trail & State Management

## Overview
Every physical state change to rental inventory in RentFlow AI MUST produce an immutable audit trail record (`InventoryTransaction`). RentFlow AI NEVER mutates inventory quantities without capturing the transaction type, quantity delta, operator, timestamp, and optional reference ID.

## Inventory Transaction Types
- `PURCHASE`: Initial procurement or fleet expansion (+ owned)
- `ADJUSTMENT`: Manual stock reconciliation count (+/- owned)
- `RESERVATION`: Quantity reserved for an upcoming event window
- `RELEASE`: Reservation cancelled or expired
- `ALLOCATE`: Equipment assigned to warehouse prep
- `CHECKOUT`: Equipment dispatched out of warehouse for delivery
- `RETURN`: Equipment returned to warehouse after event
- `DAMAGE`: Equipment damaged during event or transit (+ damaged, - available)
- `LOSS`: Equipment missing or unreturned (+ lost, - available)
- `MAINTENANCE`: Equipment sent for cleaning or maintenance (+ maintenance, - available)
- `RESTORED`: Equipment returned from maintenance to active fleet (- maintenance, + available)

## Audit Record Schema (`InventoryTransaction`)
- `id`: UUID (Primary Key)
- `tenantId`: String
- `productId`: UUID
- `transactionType`: Enum (`TransactionType`)
- `quantity`: `int`
- `referenceType`: String (`PO`, `EVENT`, `BOOKING`, `MANUAL`)
- `referenceId`: String
- `createdBy`: String (User ID or Role)
- `notes`: String
- `createdAt`: `LocalDateTime`

## Role Authorization Rules
- Direct inventory adjustments via `/api/inventory/products/{id}/adjust` are permitted ONLY for `OWNER`, `ADMIN`, and `WAREHOUSE` roles.
- `SALES`, `DRIVER`, and `CUSTOMER` roles attempting direct adjustments receive an explicit `HTTP 403 FORBIDDEN` response.
