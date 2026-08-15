# RentFlow AI — Event Operations & Rental Requirements

## Event Model Architecture

Events tie customers to inventory reservations, delivery dates, and venue execution.

### Event Model Attributes
- `id` (UUID): Primary key
- `tenantId` (String): Multi-tenant context ID
- `customerId` (UUID): Reference to parent Customer profile
- `eventName` (String): Display title (e.g., "Brown Wedding Reception")
- `eventType` (Enum): `WEDDING`, `BIRTHDAY`, `CORPORATE`, `CONFERENCE`, `FESTIVAL`, `GRADUATION`, `BABY_SHOWER`, `PRIVATE_PARTY`, `OTHER`
- `eventDate` (LocalDate): Event date
- `startTime`, `endTime` (LocalTime / String): Event schedule
- `guestCount` (Integer): Headcount
- `venueName`, `venueAddress`, `city`, `state`, `zipCode`: Location parameters
- `specialInstructions` (String): Logistics, setup, or unloading notes
- `status` (Enum): `PLANNING`, `QUOTED`, `BOOKED`, `PREPARING`, `IN_PROGRESS`, `COMPLETED`, `CANCELLED`

---

## Event Requirements (`event_requirements`)

Event requirements capture specific equipment demands (tables, chairs, linens, tents, AV gear) associated with an event.

### Requirement Entity Fields
- `id` (UUID): Primary key
- `tenantId` (String): Multi-tenant isolation key
- `eventId` (UUID): Reference to parent Event
- `description` (String): Rental item description (e.g. "Chiavari Gold Chairs")
- `quantity` (Integer): Required quantity
- `notes` (String): Specific color, size, or setup instructions

### Operations & Availability Integration
The `EventDetailComponent` includes a `[Check Product Availability]` action button that validates inventory counts across warehouse locations for scheduled event dates.
