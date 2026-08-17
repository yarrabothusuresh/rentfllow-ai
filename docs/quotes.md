# RentFlow AI — Rental Quotes Architecture

RentFlow AI provides a specialized Intelligent Rental Quote Builder designed for US event and party rental operations.

## Domain Model & Lifecycle

Quotes connect **Customer**, **Event**, **Event Requirements**, and **Products** into actionable commercial proposals:

```
[Customer + Event] ──> [Quote Draft] ──> [Availability Validation] ──> [Pricing Engine & Fees] ──> [Customer Quote Proposal]
```

### Quote Status Lifecycle

1. `DRAFT` — Initial quote draft creation.
2. `PENDING_REVIEW` — Submitted for internal approval (e.g., when discount limits are exceeded).
3. `SENT` — Officially issued to customer via email or customer portal link.
4. `VIEWED` — Opened and inspected by the customer.
5. `ACCEPTED` — Customer approved terms and deposit requirement.
6. `REJECTED` — Declined by customer.
7. `EXPIRED` — Exceeded valid period date (`validUntil`).
8. `CANCELLED` — Cancelled by sales operations.

## Quote vs. Booking vs. Inventory Reservation

> **CRITICAL ARCHITECTURAL GUARANTEE**: Creating or updating a **Quote** does **NOT** create an `InventoryReservation` record or lock physical warehouse stock.
>
> - **Quote**: Commercial proposal estimate with availability warning check.
> - **Booking**: Binding contract converting quote into confirmed order.
> - **Reservation**: Holds physical inventory units for specific event dates.

If an item quantity exceeds available stock during quote creation, the system flags `hasAvailabilityShortage: true` and presents a warning notification, allowing staff to proceed ("Continue Anyway").

## Role Security & Privacy Redaction

- `OWNER` / `ADMIN`: Full access to price overrides, discounts, profit margins, and internal notes.
- `SALES`: Allowed price overrides and discounts up to configured threshold (**Max 20%**).
- `CUSTOMER`: API responses and preview views automatically redact:
  - `internalNotes`
  - `standardUnitPrice`
  - `priceOverrideDifference`
  - Profit margins and replacement costs
