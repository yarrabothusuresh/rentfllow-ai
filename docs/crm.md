# RentFlow AI — CRM Architecture & System Guide

## Executive Overview
RentFlow AI's CRM module is built specifically for US event and party rental companies. It bridges incoming rental inquiries, customer relationship management, and event logistics execution.

```
       +--------------------+
       |   Inbound Lead     | (Web form, phone, email, partner)
       +---------+----------+
                 |
                 v  POST /api/leads/{id}/convert
       +--------------------+
       |  Lead Conversion   |  <--- Checks Duplicate Email
       +----+----------+----+       (Uses Existing or Creates CUS-XXXXXX)
            |          |
            v          v
   +------------+   +------------+
   |  Customer  |   |   Event    | (PLANNING status)
   | Profile    |   | Operations |
   +------------+   +-----+------+
                          |
                          v
                 +-------------------+
                 | Event Requirements| (Tables, Chairs, Tents)
                 +-------------------+
```

---

## 1. Domain Models & Relational Architecture

### Customer Model (`customers`)
- Primary Key: `UUID`
- Tenant Isolation: `tenantId` (String, indexed)
- Customer Number: Sequenced format (`CUS-000001`, `CUS-000002`)
- Customer Types: `INDIVIDUAL`, `BUSINESS`, `VENUE`, `EVENT_PLANNER`, `CORPORATE`, `NONPROFIT`, `OTHER`
- Email uniqueness enforced per tenant for identity tracking.

### Lead Model (`leads`)
- Primary Key: `UUID`
- Tenant Isolation: `tenantId`
- Status Pipeline: `NEW` -> `CONTACTED` -> `QUALIFIED` -> `QUOTE_REQUESTED` -> `QUOTE_SENT` -> `NEGOTIATION` -> `CONVERTED` / `LOST`
- Source Attribution: `WEBSITE`, `PHONE`, `EMAIL`, `REFERRAL`, `SOCIAL_MEDIA`, `WALK_IN`, `PARTNER`, `OTHER`

### Event Model (`events`)
- Primary Key: `UUID`
- Tenant Isolation: `tenantId`
- FK: `customerId`
- Event Types: `WEDDING`, `BIRTHDAY`, `CORPORATE`, `CONFERENCE`, `FESTIVAL`, `GRADUATION`, `BABY_SHOWER`, `PRIVATE_PARTY`, `OTHER`
- Status Pipeline: `PLANNING` -> `QUOTED` -> `BOOKED` -> `PREPARING` -> `IN_PROGRESS` -> `COMPLETED` -> `CANCELLED`

### Event Requirement Model (`event_requirements`)
- Primary Key: `UUID`
- Tenant Isolation: `tenantId`
- FK: `eventId`
- Tracks item descriptions, quantities, and operational delivery notes.

---

## 2. API Endpoints Reference

| HTTP Method | Route | Description | Role Required |
|---|---|---|---|
| `GET` | `/api/leads` | List tenant leads | `OWNER`, `ADMIN`, `SALES` |
| `POST` | `/api/leads` | Create new lead | `OWNER`, `ADMIN`, `SALES` |
| `GET` | `/api/leads/{id}` | Get lead details | `OWNER`, `ADMIN`, `SALES` |
| `PUT` | `/api/leads/{id}` | Update lead | `OWNER`, `ADMIN`, `SALES` |
| `POST` | `/api/leads/{id}/convert` | Lead conversion workflow | `OWNER`, `ADMIN`, `SALES` |
| `GET` | `/api/customers` | List tenant customers | `OWNER`, `ADMIN`, `SALES`, `WAREHOUSE`, `DRIVER` |
| `POST` | `/api/customers` | Create customer profile | `OWNER`, `ADMIN`, `SALES` |
| `GET` | `/api/customers/{id}` | Get customer profile | `OWNER`, `ADMIN`, `SALES`, `WAREHOUSE`, `DRIVER`, `CUSTOMER` |
| `PUT` | `/api/customers/{id}` | Update customer | `OWNER`, `ADMIN`, `SALES` |
| `GET` | `/api/customers/{id}/events` | Customer event history | `OWNER`, `ADMIN`, `SALES`, `WAREHOUSE`, `DRIVER`, `CUSTOMER` |
| `GET` | `/api/events` | List tenant events | `OWNER`, `ADMIN`, `SALES`, `WAREHOUSE`, `DRIVER` |
| `GET` | `/api/events/{id}` | Get event details | `OWNER`, `ADMIN`, `SALES`, `WAREHOUSE`, `DRIVER` |
| `POST` | `/api/events` | Create new event | `OWNER`, `ADMIN`, `SALES` |
| `PUT` | `/api/events/{id}` | Update event | `OWNER`, `ADMIN`, `SALES` |
| `GET` | `/api/events/{id}/requirements` | List rental requirements | `OWNER`, `ADMIN`, `SALES`, `WAREHOUSE`, `DRIVER` |
| `POST` | `/api/events/{id}/requirements` | Add requirement item | `OWNER`, `ADMIN`, `SALES` |
| `PUT` | `/api/events/{id}/requirements/{reqId}` | Update requirement item | `OWNER`, `ADMIN`, `SALES` |
| `DELETE` | `/api/events/{id}/requirements/{reqId}` | Remove requirement item | `OWNER`, `ADMIN`, `SALES` |

---

## 3. AI Copilot Integration & Security Matrix

The RentFlow AI Copilot is equipped with specialized tools to query and mutate CRM state:
- `searchLeads`: Queries lead database by name, status, or date range.
- `searchCustomers`: Locates customer profile records by name, email, or customer number (`CUS-XXXXXX`).
- `getCustomerEvents`: Retrieves active and past events for a customer.
- `getEvent`: Detailed inspection of event parameters and requirements.
- `getUpcomingEvents`: Summary of events scheduled within a date window.
- `createCustomerAction`: Structured tool request requiring human-in-the-loop approval before creating new customer entities.

Strict role isolation prevents `CUSTOMER` role users from querying other customers' data or viewing internal company statistics.
