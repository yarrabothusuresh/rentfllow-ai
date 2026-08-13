# RentFlow AI — Core Rental Business Workflow Specification

## Overview

RentFlow AI is an AI-native rental operating system designed for US event and party rental companies.
This document defines the core end-to-end rental business workflow, state machine rules, entity schemas, role-based visibility, and customer vs internal access controls.

---

## Complete 12-Stage Rental Lifecycle

```text
Customer Inquiry (01)
       ↓
02 Lead
       ↓
03 Quote
       ↓
04 Booking
       ↓
05 Inventory Reservation
       ↓
06 Warehouse Staging (Pick/Pack/Load)
       ↓
07 Delivery
       ↓
08 Event Execution
       ↓
09 Pickup
       ↓
10 Return & Inspection
       ↓
11 Payment Settlement
       ↓
12 Rental Completed & Margin Archived
```

---

## Business State Definitions

### 1. Lead Status (`LeadStatus`)
- `NEW`: Initial inquiry logged from customer, website form, or AI phone agent.
- `CONTACTED`: Sales team reached out to qualify event scope and budget.
- `QUALIFIED`: Event date, guest count, and delivery location verified.
- `QUOTE_REQUESTED`: Product requirements finalized for quotation.
- `QUOTE_SENT`: Official quote delivered to customer.
- `NEGOTIATION`: Adjustments requested on pricing, items, or delivery timing.
- `CONVERTED`: Quote accepted, contract signed, and converted into confirmed booking.
- `LOST`: Customer canceled request or selected competitor.

### 2. Quote Status (`QuoteStatus`)
- `DRAFT`: Internal proposal being drafted by Sales.
- `READY`: Approved internally and ready for customer delivery.
- `SENT`: Delivered to customer via email, link, or portal.
- `VIEWED`: Customer opened and reviewed quote details.
- `NEGOTIATION`: Customer requested item or price adjustments.
- `ACCEPTED`: Signed by customer; triggers booking generation.
- `REJECTED`: Customer explicitly declined quote.
- `EXPIRED`: Quote validity period lapsed.
- `CANCELLED`: Voided by sales team.

### 3. Booking Status (`BookingStatus`)
- `PENDING`: Awaiting signed agreement & deposit payment.
- `CONFIRMED`: Contract signed, deposit paid, inventory reserved.
- `PREPARING`: Warehouse pick/pack lists generated.
- `READY`: Items packed and staged at loading bay.
- `OUT_FOR_DELIVERY`: En route on delivery truck.
- `DELIVERED`: Delivered and set up at venue.
- `EVENT_IN_PROGRESS`: Event actively occurring.
- `READY_FOR_PICKUP`: Event ended; items breakdown complete.
- `PICKED_UP`: Loaded back onto truck by driver.
- `RETURNED`: Arrived at warehouse receiving bay.
- `INSPECTING`: Undergoing count, clean, and damage assessment.
- `COMPLETED`: Inspected, final invoice paid, inventory returned to available.
- `CANCELLED`: Canceled post-booking confirmation.

### 4. Inventory Status (`InventoryStatus`)
- `AVAILABLE`: Ready in warehouse stock pool.
- `RESERVED`: Soft-locked for confirmed future booking.
- `ALLOCATED`: Hard-assigned to specific order pick list.
- `IN_WAREHOUSE`: Currently physically located in warehouse.
- `LOADED`: Staged onto delivery truck.
- `OUT_ON_RENT`: In possession of customer at venue.
- `RETURNED`: Arrived back at warehouse.
- `INSPECTION`: Undergoing post-rental inspection.
- `MAINTENANCE`: Cleaning, repair, or reconditioning.
- `DAMAGED`: Damaged beyond immediate reuse.
- `LOST`: Unreturned or lost in transit.

### 5. Payment Status (`PaymentStatus`)
- `PENDING`: Invoice issued; no payment received yet.
- `PARTIALLY_PAID`: Initial deposit paid (e.g., 25% to lock booking).
- `PAID`: Paid in full.
- `FAILED`: Payment processing transaction failed.
- `REFUNDED`: Security deposit or cancellation refund processed.
- `OVERDUE`: Past agreed payment due date.

### 6. Delivery Status (`DeliveryStatus`)
- `SCHEDULED`: Delivery window reserved.
- `ASSIGNED`: Truck and driver assigned to route.
- `LOADED`: Cargo verified on vehicle.
- `OUT_FOR_DELIVERY`: Driver en route to venue.
- `ARRIVED`: Truck arrived at customer address.
- `DELIVERED`: Unloaded, inspected, and signed off.
- `PICKUP_SCHEDULED`: Post-event pickup time set.
- `PICKED_UP`: Items retrieved from venue.
- `COMPLETED`: Route finished and verified.
- `CANCELLED`: Delivery route voided.

### 7. Event Status (`EventStatus`)
- `PLANNING`: Event concept defined; gathering requirements.
- `QUOTED`: Active quote attached.
- `BOOKED`: Confirmed event date and locked reservation.
- `PREPARING`: Fulfillment logistics active.
- `IN_PROGRESS`: Event active.
- `COMPLETED`: Event ended; post-event teardown complete.
- `CANCELLED`: Event canceled.

---

## State Transition Rules (`WorkflowStateMachine`)

### Allowed Quote Transitions
- `DRAFT` → `READY`, `SENT`, `CANCELLED`
- `READY` → `SENT`, `CANCELLED`
- `SENT` → `VIEWED`, `NEGOTIATION`, `ACCEPTED`, `REJECTED`, `EXPIRED`, `CANCELLED`
- `VIEWED` → `NEGOTIATION`, `ACCEPTED`, `REJECTED`, `EXPIRED`, `CANCELLED`
- `NEGOTIATION` → `ACCEPTED`, `REJECTED`, `CANCELLED`
- `ACCEPTED` → *(Terminal stage; cannot revert to DRAFT)*

### Rejected Quote Transitions
- `ACCEPTED` → `DRAFT` ❌
- `ACCEPTED` → `SENT` ❌
- `REJECTED` → `DRAFT` ❌

### Core Business Rules
1. **Rule 1 (Quote Acceptance):** A quote must not become a confirmed booking until customer explicitly accepts it.
2. **Rule 2 (Inventory Reservation):** Inventory is hard-reserved upon booking confirmation.
3. **Rule 3 (Warehouse Staging):** Pick and pack tickets generate only for confirmed bookings.
4. **Rule 4 (Delivery Scheduling):** Trucks and drivers are dispatched against confirmed bookings.
5. **Rule 5 (Inspection Gate):** Returned inventory must pass inspection before returning to `AVAILABLE` status.
6. **Rule 6 (Damage Handling):** Damaged inventory transitions to `DAMAGED` / `MAINTENANCE`, not directly to `AVAILABLE`.
7. **Rule 7 (Financial Completion):** A booking cannot reach `COMPLETED` status until outstanding invoices are settled and inspection is cleared.
8. **Rule 8 (Tenant Isolation):** Customer users can view only their own tenant events and bookings.
9. **Rule 9 (Cost/Margin Secrecy):** Internal unit costs and profit margins must NEVER be exposed to customer accounts.
10. **Rule 10 (Audit Log):** Every state transition captures `entity`, `entityId`, `fromStatus`, `toStatus`, `changedBy`, `changedAt`, and `reason`.

---

## Role-Based Responsibilities

| Role | Lifecycle Focus | Visibility | Key Actions |
|---|---|---|---|
| **OWNER** | Entire Lifecycle (01-12) | Full Internal Access | Financial audit, margin review, override approvals |
| **ADMIN** | System Operations & Config | Full Internal Access | User management, inventory settings, profile edits |
| **SALES** | 01 Inquiry → 04 Booking | Pipeline & Quotes | Create quote, send follow-ups, convert bookings |
| **WAREHOUSE**| 05 Inventory → 06 Warehouse → 10 Return | Warehouse & Stock | Pick list execution, staging, return inspection |
| **DRIVER** | 07 Delivery & 09 Pickup | Routes & Locations | Navigation, delivery sign-off, venue pickup |
| **CUSTOMER** | 03 Quote, 04 Booking, 08 Event, 11 Payment | Customer View Only | Accept quote, view schedule, pay balance |
