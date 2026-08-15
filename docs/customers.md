# RentFlow AI — Customers Module Specification

## Customer Profile Management

Customers represent established client relationships in RentFlow AI. Profiles are maintained per tenant and support both individual event hosts and corporate/venue accounts.

### Customer Entity Properties
- `id` (UUID): Unique primary key
- `tenantId` (String): Multi-tenant isolation key
- `customerNumber` (String): Human-readable sequential ID (`CUS-000001`, `CUS-000002`)
- `firstName`, `lastName` (String): Primary contact person
- `companyName` (String): Business or Organization name
- `email` (String): Primary email address (Indexed per tenant)
- `phone`, `alternatePhone` (String): Phone contact info
- `customerType` (Enum): `INDIVIDUAL`, `BUSINESS`, `VENUE`, `EVENT_PLANNER`, `CORPORATE`, `NONPROFIT`, `OTHER`
- `status` (Enum): `ACTIVE`, `INACTIVE`, `BLOCKED`
- `billingAddress`, `shippingAddress`, `city`, `state`, `zipCode`, `country`: Address records
- `notes` (String): Internal account preferences and history

---

## Sequential Customer Number Generation

Customer numbers are generated dynamically per tenant during creation:
1. System checks the count of existing customer records for `tenantId`.
2. Format pattern `CUS-%06d` produces zero-padded sequential IDs (`CUS-000001`, `CUS-000002`).
3. Unique database constraints ensure no customer number collisions occur within tenant scopes.

---

## Event History Integration

Each customer profile is linked to their complete event lifecycle history via `GET /api/customers/{id}/events`. Staff can view upcoming wedding rentals, corporate galas, and past completed bookings directly from the customer workspace.
