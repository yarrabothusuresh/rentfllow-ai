# Inventory Reservations Lifecycle & Model

## Overview
`InventoryReservation` records lock specific asset quantities for confirmed quotes or booked events across exact start and end datetimes.

## Reservation Schema
- `id`: UUID (Primary Key)
- `tenantId`: String (Multi-tenant isolation)
- `productId`: UUID
- `eventId`: UUID (Optional link to Event)
- `bookingId`: UUID (Optional link to Booking)
- `quantity`: `int`
- `startDateTime`: `LocalDateTime`
- `endDateTime`: `LocalDateTime`
- `status`: Enum (`PENDING`, `RESERVED`, `RELEASED`, `CANCELLED`)

## Lifecycle States
1. `PENDING`: Draft quote or held inventory awaiting customer confirmation.
2. `RESERVED`: Confirmed booking; quantity is locked and deducted from date-window availability.
3. `RELEASED`: Event completed and equipment returned to available warehouse status.
4. `CANCELLED`: Order cancelled; reservation released back to public availability.
