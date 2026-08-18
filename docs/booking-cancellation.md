# Booking Cancellation & Inventory Release

## Overview
Booking cancellation safely reverses inventory commitments, restores availability, and logs complete audit trails.

## Cancellation Process
1. Permission check (OWNER, ADMIN, SALES allowed; WAREHOUSE and DRIVER restricted).
2. Status update: Booking status changed to `CANCELLED`.
3. Inventory release: Linked `InventoryReservation` records marked `RELEASED`.
4. Transaction logging: `InventoryTransaction` of type `RELEASE` logged for each released product.
5. Instant availability restoration: Recalculated available quantity immediately reflects released inventory.

## Transactional Integrity
Executed within a single `@Transactional` boundary. If any step fails, the entire transaction rolls back to prevent orphaned reservations or incomplete status updates.
