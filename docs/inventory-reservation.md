# Inventory Reservation & Date-Based Availability Model

## Overview
Inventory Reservation in RentFlow AI links confirmed bookings to product availability across specific event date ranges.

## Inventory Reservation Entity (`inventory_reservations`)
- `id` (UUID)
- `tenantId` (String)
- `productId` (UUID)
- `eventId` (UUID)
- `bookingId` (UUID)
- `quantity` (int)
- `startDateTime` (LocalDateTime)
- `endDateTime` (LocalDateTime)
- `status` (`ReservationStatus`: PENDING, RESERVED, RELEASED, CANCELLED)
- `createdAt`, `updatedAt`

## Date-Based Availability Formula
For any given product and date range `(requestedStart, requestedEnd)`:

```
Reserved Quantity = SUM(quantity) of all reservations where:
   tenantId = currentTenant
   productId = targetProduct
   status IN ('RESERVED', 'PENDING')
   startDateTime < requestedEnd AND endDateTime > requestedStart

Available Quantity = max(0, quantityOwned - quantityInMaintenance - quantityDamaged - quantityLost - Reserved Quantity)
```

## Inventory Transaction Audit Trail (`inventory_transactions`)
Every inventory lifecycle change creates an immutable audit trail entry:
- Transaction Types: `RESERVATION`, `RELEASE`, `ALLOCATE`, `CHECKOUT`, `RETURN`, `DAMAGE`, `LOSS`.
- Fields: `id`, `tenantId`, `productId`, `transactionType`, `quantity`, `referenceType` ("BOOKING"), `referenceId`, `notes`, `createdBy`, `createdAt`.
