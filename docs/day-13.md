# RentFlow AI — Day 13 Architecture & Release Documentation

## Module: Customer Portal

### 1. Objective
Implement the complete **Customer Portal** for RentFlow AI. The customer portal provides rental customers with a secure, self-service dashboard to view their events, proposals/quotes, bookings, invoices, payment history, and delivery details. Customers can accept proposals, request quote changes, submit support inquiries, and manage their contact profile. The system enforces strict tenant isolation, object-level authorization, and field masking to prevent customers from accessing another customer's data or internal company secrets (cost prices, margins, internal notes, inventory levels).

---

### 2. Customer Account Model & Identity

```
    [ Tenant ]
        │
        ├──► [ Customer ] (Business entity: ABC Events LLC)
        │         │
        │         └──► [ CustomerUser ] (Portal Identity: customer@abcevents.demo)
        │                   │
        │                   └──► [ User ] (Auth Principal with Role: CUSTOMER)
```

- Each `CustomerUser` belongs to **ONE Tenant** and **ONE Customer Account**.
- Cross-tenant or cross-customer mapping is strictly forbidden.

---

### 3. RBAC & Security Boundaries

#### `CUSTOMER` Role Permission Matrix:
- **Allowed Permissions**: `VIEW_OWN_PROFILE`, `VIEW_OWN_EVENTS`, `VIEW_OWN_QUOTES`, `ACCEPT_OWN_QUOTES`, `REQUEST_QUOTE_CHANGES`, `VIEW_OWN_BOOKINGS`, `VIEW_OWN_INVOICES`, `VIEW_OWN_PAYMENTS`, `VIEW_DELIVERY_STATUS`, `CREATE_CUSTOMER_REQUEST`.
- **Explicitly Forbidden**: `CREATE_PRODUCT`, `EDIT_PRODUCT`, `VIEW_INVENTORY`, `VIEW_COST`, `VIEW_MARGIN`, `VIEW_INTERNAL_NOTES`, `RECORD_PAYMENT`, `VOID_PAYMENT`, `VOID_INVOICE`, `MANAGE_USERS`, `MANAGE_TENANT`.

#### Field Masking Guarantee:
Customer-facing DTOs (`CustomerPortalQuoteDTO`, `CustomerPortalBookingDTO`, etc.) systematically exclude:
- `costPrice`, `standardUnitPrice`, `margin`
- `internalNotes`
- `inventoryReservations` (exact warehouse serials/quantity buffers)
- `createdBy` (internal employee names/roles)

---

### 4. Database Schema Changes

#### `customer_users` Table
| Column | Type | Constraints | Description |
|---|---|---|---|
| `id` | UUID | PRIMARY KEY | Unique customer user identifier |
| `tenant_id` | VARCHAR | NOT NULL, INDEXED | Multi-tenant isolation ID |
| `customer_id` | UUID | NOT NULL, INDEXED | Associated Customer account ID |
| `user_id` | UUID | NOT NULL, INDEXED | Associated User system identity |
| `email` | VARCHAR | NOT NULL, UNIQUE (per tenant) | Login email address |
| `password_hash` | VARCHAR | NOT NULL | Password credential |
| `active` | BOOLEAN | NOT NULL | Account active status |
| `created_at` | TIMESTAMP | NOT NULL | Account creation timestamp |
| `updated_at` | TIMESTAMP | NOT NULL | Last update timestamp |

#### `customer_requests` Table
| Column | Type | Constraints | Description |
|---|---|---|---|
| `id` | UUID | PRIMARY KEY | Unique request identifier |
| `tenant_id` | VARCHAR | NOT NULL, INDEXED | Multi-tenant isolation ID |
| `customer_id` | UUID | NOT NULL, INDEXED | Associated Customer account ID |
| `request_type` | VARCHAR | NOT NULL | Enum: `QUOTE_CHANGE`, `DELIVERY_QUESTION`, `BOOKING_QUESTION`, `BILLING_QUESTION`, `GENERAL` |
| `subject` | VARCHAR | NOT NULL | Request subject title |
| `message` | VARCHAR(2000) | NOT NULL | Request message body |
| `status` | VARCHAR | NOT NULL, INDEXED | Enum: `OPEN`, `IN_PROGRESS`, `RESOLVED`, `CLOSED` |
| `created_at` | TIMESTAMP | NOT NULL | Creation timestamp |
| `updated_at` | TIMESTAMP | NOT NULL | Last update timestamp |

---

### 5. REST API Specification (`/api/portal/**`)

| Method | Path | Request Body | Allowed Roles | Description |
|---|---|---|---|---|
| `POST` | `/api/portal/auth/login` | `{ "email": "...", "password": "..." }` | Public | Customer portal login |
| `GET` | `/api/portal/dashboard` | None | `CUSTOMER` | Retrieve customer dashboard metrics & upcoming event banner |
| `GET` | `/api/portal/profile` | None | `CUSTOMER` | Retrieve customer profile details |
| `PUT` | `/api/portal/profile` | `CustomerProfileDTO` | `CUSTOMER` | Update safe customer contact details & billing address |
| `GET` | `/api/portal/events` | None | `CUSTOMER` | List customer's events |
| `GET` | `/api/portal/events/{id}` | None | `CUSTOMER` | Retrieve customer-safe event detail |
| `GET` | `/api/portal/quotes` | None | `CUSTOMER` | List shared quotes (excluding internal draft status) |
| `GET` | `/api/portal/quotes/{id}` | None | `CUSTOMER` | Retrieve customer-safe proposal detail |
| `POST` | `/api/portal/quotes/{id}/accept` | None | `CUSTOMER` | Accept proposal (transitions quote to `ACCEPTED`) |
| `POST` | `/api/portal/quotes/{id}/request-changes` | `{ "message": "..." }` | `CUSTOMER` | Request quote changes (transitions to `CHANGE_REQUESTED`) |
| `GET` | `/api/portal/bookings` | None | `CUSTOMER` | List customer's bookings |
| `GET` | `/api/portal/bookings/{id}` | None | `CUSTOMER` | Retrieve customer-safe booking detail |
| `GET` | `/api/portal/invoices` | None | `CUSTOMER` | List customer's invoices |
| `GET` | `/api/portal/invoices/{id}` | None | `CUSTOMER` | Retrieve customer-safe invoice detail |
| `GET` | `/api/portal/invoices/{id}/payments` | None | `CUSTOMER` | Retrieve payment history for invoice |
| `GET` | `/api/portal/requests` | None | `CUSTOMER` | List customer support requests |
| `POST` | `/api/portal/requests` | `CreateCustomerRequestDTO` | `CUSTOMER` | Submit new support inquiry or quote change message |

---

### 6. Security Enforcement & Object-Level Authorization
Every request to `/api/portal/**` resolves the customer ID directly from the authenticated principal or `X-Customer-Id` session header.
The backend evaluates:
`if (!resource.getTenantId().equals(userTenantId) || !resource.getCustomerId().equals(userCustomerId))` -> Return `HTTP 403 Forbidden`.

Untrusted client parameters (`customerId`, `tenantId` in request bodies or query params) are strictly ignored in favor of server-resolved session credentials.

---

### 7. Audit Logging
The portal records audit events using the existing audit architecture:
- `CUSTOMER_LOGIN`: Recorded upon successful portal login.
- `QUOTE_VIEWED`: Recorded when quote detail is opened.
- `QUOTE_ACCEPTED`: Recorded when quote is accepted by customer.
- `QUOTE_CHANGE_REQUESTED`: Recorded when customer requests quote changes.
- `BOOKING_VIEWED`: Recorded when booking detail is viewed.
- `INVOICE_VIEWED`: Recorded when invoice detail is viewed.
- `CUSTOMER_REQUEST_CREATED`: Recorded when a new support message is submitted.

---

### 8. Demo Credentials
- **Customer A**:
  - Email: `customer@abcevents.demo`
  - Password: `demo`
  - Company: ABC Events LLC / Brown Wedding (Emily Brown)
- **Customer B** (Security Testing):
  - Email: `customer.b@xyzevents.demo`
  - Password: `demo`
  - Company: XYZ Events Inc.
