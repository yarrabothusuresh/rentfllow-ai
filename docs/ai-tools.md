# RentFlow AI — AI Tools Specification

This document details all conceptual and concrete business tools implemented in RentFlow AI Day 6.

---

## Tool Summary Matrix

| Tool Name | Description | Allowed Roles | Multi-Tenant Check | Requires Approval |
| :--- | :--- | :--- | :--- | :--- |
| `searchCustomer` | Search customer records by name or query string | OWNER, ADMIN, SALES, CUSTOMER | Yes | No |
| `getCustomer` | Retrieve customer details by ID | OWNER, ADMIN, SALES, CUSTOMER | Yes | No |
| `searchProducts` | Search catalog products by name/category and quantity | OWNER, ADMIN, SALES, WAREHOUSE, CUSTOMER | Yes | No |
| `checkAvailability` | Check inventory availability for product, quantity, and event date | OWNER, ADMIN, SALES, WAREHOUSE, CUSTOMER | Yes | No |
| `getBooking` | Retrieve booking details by booking ID | OWNER, ADMIN, SALES, WAREHOUSE, DRIVER, CUSTOMER | Yes | No |
| `calculateQuote` | Calculate quote pricing breakdown (Rental, Delivery, Setup, Total) | OWNER, ADMIN, SALES, CUSTOMER | Yes | No |
| `calculateProfitability` | Calculate revenue, cost, profit, and margin (Demo Data) | OWNER, ADMIN, SALES | Yes | No |
| `getUpcomingBookings` | Retrieve upcoming bookings and event schedule | OWNER, ADMIN, SALES, WAREHOUSE, DRIVER, CUSTOMER | Yes | No |
| `getWarehouseTasks` | Retrieve warehouse pick, pack, load, and return tasks | OWNER, ADMIN, WAREHOUSE | Yes | No |
| `getDeliveries` | Retrieve delivery jobs, driver assignments, and routes | OWNER, ADMIN, SALES, WAREHOUSE, DRIVER | Yes | No |
| `sendPaymentReminder` | Action tool to send payment reminder to customer | OWNER, ADMIN, SALES | Yes | **Yes** |
| `createQuote` | Action tool to generate a new customer quote | OWNER, ADMIN, SALES | Yes | **Yes** |
| `sendQuote` | Action tool to send quote to customer | OWNER, ADMIN, SALES | Yes | **Yes** |
| `reserveInventory` | Action tool to reserve inventory items for booking | OWNER, ADMIN, SALES, WAREHOUSE | Yes | **Yes** |
| `createBooking` | Action tool to convert quote to confirmed booking | OWNER, ADMIN, SALES | Yes | **Yes** |
| `assignDriver` | Action tool to assign driver/vehicle to delivery job | OWNER, ADMIN, WAREHOUSE | Yes | **Yes** |
| `createWarehouseTask` | Action tool to generate pick/pack list tickets | OWNER, ADMIN, WAREHOUSE | Yes | **Yes** |

---

## Concrete Tool Specifications

### 1. `searchCustomer`
- **Input**: `{"query": "Emily Brown"}`
- **Output**: `{"customerId": "customer-001", "name": "Emily Brown", "city": "Dallas", "state": "TX", "activeBookings": 1}`

### 2. `getCustomer`
- **Input**: `{"customerId": "customer-001"}`
- **Output**: Complete customer profile including contact information, active bookings, and rental history.

### 3. `searchProducts`
- **Input**: `{"query": "chairs", "quantity": 250}`
- **Output**: List of matching products (e.g. 250 Chiavari Chairs, Available: 300, Price: $8.00).

### 4. `checkAvailability`
- **Input**: `{"productId": "chair-001", "quantity": 250, "eventDate": "2026-09-20"}`
- **Output**: `{"available": true, "requested": 250, "availableQuantity": 300, "eventDate": "2026-09-20"}`

### 5. `getBooking`
- **Input**: `{"bookingId": "booking-001"}`
- **Output**: Complete booking information (Customer: Emily Brown, Wedding, Sept 20, 2026, Status: QUOTE_SENT_AWAITING_CONFIRMATION).

### 6. `calculateQuote`
- **Input**: `{"eventType": "Wedding", "guestCount": 250}`
- **Output**: Rental: $4,850, Delivery: $750, Setup: $400, Total: $6,000.

### 7. `calculateProfitability`
- **Input**: `{"bookingId": "booking-001"}`
- **Output**: Revenue: $6,480, Estimated Cost: $2,920, Profit: $3,560, Margin: 54.9% (Labeled clearly as Demo Data).

### 8. `getUpcomingBookings`
- **Input**: `tenantId`
- **Output**: List of upcoming bookings and event schedules.

### 9. `getWarehouseTasks`
- **Input**: `tenantId`
- **Output**: Today's Pick Lists, Pack Lists, Load Lists, and Returns Inspections.

### 10. `getDeliveries`
- **Input**: `tenantId`
- **Output**: Delivery schedule, driver assignments, truck routes, and delivery status.

### 11. `sendPaymentReminder` (Action Tool)
- **Input**: `{"customer": "Emily Brown", "bookingId": "booking-001"}`
- **Output**: `status: "ACTION_REQUIRES_APPROVAL"`, preview message, outstanding amount ($2,500). Requires human approval in UI.
