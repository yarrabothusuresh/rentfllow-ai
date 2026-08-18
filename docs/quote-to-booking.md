# Quote to Booking Conversion & Customer Acceptance Flow

## Overview
This document specifies the transition from commercial Quote to confirmed Booking within RentFlow AI.

## Flow Sequence
```
QUOTE (DRAFT / SENT / VIEWED)
   ↓
CUSTOMER ACCEPTS (via /api/quotes/{id}/accept or Preview UI)
   ↓
QUOTE STATUS -> ACCEPTED
   ↓
AVAILABILITY DOUBLE-CHECK (Transactional check of every item for event dates)
   ↓
IF SHORTAGE:
   STOP -> Return HTTP 409 BOOKING_UNAVAILABLE with shortage breakdown
IF AVAILABLE:
   CREATE BOOKING -> Copy items (Price Snapshot)
   CREATE INVENTORY RESERVATION -> Status: RESERVED
   LOG INVENTORY TRANSACTION -> Type: RESERVATION
   UPDATE BOOKING STATUS -> CONFIRMED
```

## Customer Acceptance API
- Endpoint: `POST /api/quotes/{quoteId}/accept`
- Request: `{ "accepted": true }`
- Updates quote status to `ACCEPTED`.

## Booking Creation Endpoint
- Endpoint: `POST /api/bookings/from-quote/{quoteId}`
- Request: `{ "confirmation": true }`
- Returns `BookingDTO` on success or `BookingUnavailableDTO` on shortage.
