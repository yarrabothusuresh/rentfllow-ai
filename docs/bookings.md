# Booking Management Architectural Specification

## Overview
Booking Management in RentFlow AI handles confirmed rental agreements and commits inventory reservations.

## Critical Business Rule
```
QUOTE != BOOKING != INVENTORY RESERVATION

- Quote: Commercial price quote / proposal. Does NOT lock or reserve inventory.
- Booking: Confirmed rental contract.
- Inventory Reservation: Commitment of physical warehouse inventory for specific event dates.
```

## Data Schema & Entities

### Booking Entity (`booking`)
- `id` (UUID, Primary Key)
- `tenantId` (String, Multi-Tenancy Index)
- `bookingNumber` (String, Format `BKG-000001`, Unique)
- `quoteId` (UUID, FK to Quote)
- `customerId` (UUID, FK to Customer)
- `eventId` (UUID, FK to Event)
- `status` (`BookingStatus`: PENDING, CONFIRMED, DEPOSIT_PENDING, PARTIALLY_PAID, PAID, READY_FOR_FULFILLMENT, IN_PROGRESS, COMPLETED, CANCELLED, NO_SHOW)
- `bookingDate` (LocalDate)
- `rentalStartDateTime` (LocalDateTime)
- `rentalEndDateTime` (LocalDateTime)
- Monetary fields (`subtotal`, `discountAmount`, `deliveryFee`, `pickupFee`, `setupFee`, `breakdownFee`, `serviceFee`, `taxAmount`, `totalAmount`, `depositRequired`, `depositPaid`, `balanceDue` using `BigDecimal`)
- `notes`, `internalNotes`, `createdBy`, `createdAt`, `updatedAt`

### BookingItem Entity (`booking_item`)
Snapshots item details at booking confirmation to preserve historical record against catalog price modifications:
- `id` (UUID)
- `bookingId` (UUID)
- `productId` (UUID)
- `description` (String)
- `quantity` (int)
- `unitPrice` (BigDecimal)
- `rentalStartDateTime`, `rentalEndDateTime`
- `lineSubtotal` (BigDecimal)

## REST APIs
- `POST /api/bookings/from-quote/{quoteId}`: Converts accepted/sent quote to booking with mandatory availability double-check.
- `POST /api/bookings/{bookingId}/confirm`: Confirms pending booking and creates firm inventory reservations.
- `POST /api/bookings/{bookingId}/cancel`: Cancels booking, releases inventory reservations, and logs `RELEASE` inventory transaction audit trail.
- `GET /api/bookings`: Lists tenant bookings.
- `GET /api/bookings/{id}`: Fetches detailed booking with item snapshots and reserved inventory breakdown.
