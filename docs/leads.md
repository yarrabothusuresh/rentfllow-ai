# RentFlow AI — Leads Management Specification

## Lead Management Architecture
Leads represent initial rental inquiries submitted via online storefronts, phone inquiries, email requests, or referral channels.

### Lead Data Fields
- `id` (UUID): Unique ID
- `tenantId` (String): Multi-tenant context identifier
- `firstName`, `lastName` (String): Contact details
- `companyName` (String): Optional business entity name
- `email` (String): Primary contact email
- `phone` (String): Contact phone number
- `source` (Enum): `WEBSITE`, `PHONE`, `EMAIL`, `REFERRAL`, `SOCIAL_MEDIA`, `WALK_IN`, `PARTNER`, `OTHER`
- `status` (Enum): `NEW`, `CONTACTED`, `QUALIFIED`, `QUOTE_REQUESTED`, `QUOTE_SENT`, `NEGOTIATION`, `CONVERTED`, `LOST`
- `eventType` (Enum): `WEDDING`, `BIRTHDAY`, `CORPORATE`, `CONFERENCE`, `FESTIVAL`, `GRADUATION`, `BABY_SHOWER`, `PRIVATE_PARTY`, `OTHER`
- `eventDate` (LocalDate): Projected event date
- `guestCount` (Integer): Estimated attendee count
- `venueName` (String): Projected venue
- `notes` (String): Customer specifications or requirements
- `assignedTo` (String): Assigned sales representative

---

## Lead Conversion Workflow

When a sales representative qualifies a lead, invoking `POST /api/leads/{id}/convert` initiates an automated transaction:

1. **Duplicate Email Detection**: Checks the `customers` repository for existing profiles matching `lead.email`.
2. **Duplicate Handling**:
   - If a duplicate customer is found and `forceNewCustomer=false` (and no `useExistingCustomerId` is provided), the API returns status `POSSIBLE_DUPLICATE_FOUND` with the candidate `CustomerDTO`.
   - If `useExistingCustomerId` is supplied, the lead is linked to that existing customer.
   - If `forceNewCustomer=true` or no duplicate exists, a new Customer profile is created with a auto-generated `CUS-XXXXXX` number.
3. **Event Generation**: A new Event record is initialized in `PLANNING` status, populated with `eventType`, `eventDate`, `guestCount`, `venueName`, and `notes`.
4. **Requirement Population**: Default rental requirement placeholders (e.g. Chairs, Tables, Tents based on `guestCount` and `eventType`) are inserted into `event_requirements`.
5. **Lead Status Update**: The lead's status is updated to `CONVERTED`.
