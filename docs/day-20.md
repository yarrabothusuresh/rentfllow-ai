# Day 20 — Customer Experience, Self-Service Portal & Online Booking

## 1. Objective
To build a professional, responsive, and seamless Customer Experience layer for RentFlow AI SaaS. This self-service portal and public booking engine allows rental customers to:
1. Browse products and check real-time date availability on a public storefront.
2. Build a rental cart, validate date-based inventory availability, and submit quote requests or proceed through online checkout.
3. Register and log in to a dedicated Customer Self-Service Portal (`/portal`).
4. View and manage bookings, quotes, invoices, payment history, saved addresses, and profile information.
5. Approve or decline quotes (with inventory reservation upon approval and reason recording upon decline).
6. Review damage claims (customer-safe estimates with Approve/Dispute options) from post-rental returns.
7. Communicate directly with rental company staff via a simple threaded messaging system.
8. View real-time rental lifecycle progression (CONFIRMED → PREPARING → DELIVERED → OUT ON RENT → PICKUP SCHEDULED → PICKED UP → CHECK-IN → INSPECTION → COMPLETED).
9. Allow staff to manage customer requests via a Staff Customer Request Dashboard (`/customer-requests`) and inspect a Customer 360 View (`/customers/:id`).

---

## 2. Existing Architecture Findings & Integration
- **Customer Architecture**: `Customer` entity (`com.rentflow.ai.model.Customer`) stores core customer data. `CustomerUser` (`com.rentflow.portal.model.CustomerUser`) maps portal user credentials to a `Customer` record.
- **Product Catalog**: `Product` (`com.rentflow.ai.model.Product`) and `ProductCategory` entities manage rental inventory pricing, images, categories, and inventory totals (`quantityOwned`, `quantityInMaintenance`, `quantityDamaged`, `quantityLost`).
- **Availability Engine**: `AvailabilityService` (`com.rentflow.ai.service.AvailabilityService`) dynamically calculates net available quantity for a date range by evaluating total owned inventory minus maintenance, damaged, lost, and overlapping `InventoryReservation` records.
- **Quote & Booking Flow**: `QuoteService` calculates line subtotals, discounts, taxes, and fees. `BookingService` converts accepted quotes into `Booking` records, creating corresponding `InventoryReservation` and `InventoryTransaction` entries.
- **Invoice & Payment Flow**: `InvoiceService` creates invoices from bookings/quotes. `PaymentService` processes payment records (`Payment`) linked to invoices/bookings.
- **Damage Claims**: `DamageClaimService` (`com.rentflow.claims.service.DamageClaimService`) tracks post-return inspection damages and customer-safe claim estimates.
- **Notification Flow**: `NotificationService` handles event-driven notifications (`QUOTE_CREATED`, `QUOTE_APPROVED`, `BOOKING_CONFIRMED`, `PAYMENT_RECEIVED`, `DELIVERY_SCHEDULED`, `DAMAGE_CLAIM_CREATED`, etc.).
- **Tenant Isolation & Security**: Strictly enforced across all database queries, services, and endpoints using `tenantId` filtering and RBAC permissions (`CUSTOMER`, `SALES`, `OPERATIONS_MANAGER`, `FINANCE`, `WAREHOUSE`, `DRIVER`, `OWNER`).

---

## 3. Domain Model Extensions
```
TenantStorefrontConfig (1) ─── (N) Product
Customer (1) ─── (N) CustomerUser
Customer (1) ─── (N) CustomerAddress
Customer (1) ─── (N) CustomerConversation ─── (N) CustomerMessage
Customer (1) ─── (N) CustomerActivity
RentalCart (1) ─── (N) RentalCartItem
```

### New Domain Models:
1. `CustomerAddress`: Saved delivery and billing addresses (`HOME`, `OFFICE`, `VENUE`, `OTHER`).
2. `RentalCart` & `RentalCartItem`: Session/token-based cart storage with 30-minute expiration.
3. `CustomerConversation` & `CustomerMessage`: Threaded customer-staff messaging system (`OPEN`, `WAITING_FOR_CUSTOMER`, `WAITING_FOR_STAFF`, `CLOSED`).
4. `CustomerActivity`: Event logging for customer journey analysis (`CATALOG_VIEW`, `PRODUCT_VIEW`, `QUOTE_REQUESTED`, `QUOTE_APPROVED`, `BOOKING_CREATED`, `PAYMENT_COMPLETED`, `MESSAGE_SENT`, `CLAIM_APPROVED`, `CLAIM_DISPUTED`).
5. `TenantStorefrontConfig`: Tenant-specific branding, slug, company logo, phone, email, terms, currency.

---

## 4. Customer Journey Lifecycle
$$\text{PUBLIC CATALOG} \rightarrow \text{DATE & AVAILABILITY CHECK} \rightarrow \text{CART} \rightarrow \text{QUOTE REQUEST / CHECKOUT} \rightarrow \text{CUSTOMER PORTAL} \rightarrow \text{APPROVE QUOTE} \rightarrow \text{BOOKING CONFIRMED} \rightarrow \text{DELIVERY & PICKUP} \rightarrow \text{RETURN & CLAIM REVIEW}$$

---

## 5. Security & RBAC Matrix
| Role | Public Catalog | Portal Dashboard | Approve/Decline Quote | Manage Addresses | Damage Claim Review | Staff Requests Dashboard | Customer 360 View |
| :--- | :---: | :---: | :---: | :---: | :---: | :---: | :---: |
| **CUSTOMER** | Read-Only | Own Data Only | Own Quotes Only | Own Addresses | Own Claims (Approve/Dispute) | ❌ | ❌ |
| **SALES** | Read-Only | ❌ | ❌ (View/Send) | View Customer | View Estimates | ✅ | ✅ |
| **OPERATIONS_MANAGER** | Read-Only | ❌ | ❌ | View Customer | Manage Claims | ✅ | ✅ |
| **FINANCE** | Read-Only | ❌ | ❌ | View Customer | View Charges | ✅ | ✅ |
| **WAREHOUSE / DRIVER** | ❌ | ❌ | ❌ | View Delivery | ❌ | Operational Only | Operational Only |
| **OWNER / ADMIN** | Read-Only | Full Admin | Admin Override | Full Access | Full Access | ✅ | ✅ |

---

## 6. API Endpoints Overview

### Public Storefront APIs (`/api/public/...`)
- `GET /api/public/storefront/{tenantSlug}`: Public storefront branding config.
- `GET /api/public/catalog`: Paginated product catalog with category, price, date availability filters and sorting.
- `GET /api/public/catalog/{id}`: Detailed public product information.
- `GET /api/public/catalog/{id}/availability`: Server-side real-time availability check.
- `GET /api/public/cart`: Retrieve current cart items.
- `POST /api/public/cart/items`: Add product to cart.
- `PATCH /api/public/cart/items/{id}`: Update cart item quantity or rental dates.
- `DELETE /api/public/cart/items/{id}`: Remove item from cart.
- `POST /api/public/cart/validate`: Validate cart items for availability and server-side pricing.
- `POST /api/public/quote-requests`: Submit a new quote request from cart or guest form.

### Customer Portal APIs (`/api/portal/...`)
- `POST /api/portal/auth/register`: Customer account registration.
- `POST /api/portal/auth/login`: Customer portal authentication.
- `GET /api/portal/dashboard`: Customer portal metrics & recent activity summary.
- `GET /api/portal/bookings` & `GET /api/portal/bookings/{id}`: Customer bookings and detail view.
- `GET /api/portal/quotes` & `GET /api/portal/quotes/{id}`: Customer quotes list and detail.
- `POST /api/portal/quotes/{id}/approve`: Customer quote approval with inventory reservation.
- `POST /api/portal/quotes/{id}/decline`: Customer quote decline with reason.
- `GET /api/portal/invoices` & `GET /api/portal/invoices/{id}`: Customer invoices and detail.
- `GET /api/portal/payments`: Customer payment history.
- `GET /api/portal/claims` & `GET /api/portal/claims/{id}`: Customer damage claims list & detail.
- `POST /api/portal/claims/{id}/approve` & `POST /api/portal/claims/{id}/dispute`: Approve/dispute claim estimate.
- `GET /api/portal/messages` & `POST /api/portal/messages`: Threaded customer messaging.
- `GET /api/portal/addresses` & `POST /api/portal/addresses`: Customer saved address management.
- `GET /api/portal/profile` & `PATCH /api/portal/profile`: Customer profile management.

### Staff Management APIs (`/api/...`)
- `GET /api/customer-requests/dashboard`: Staff customer requests summary.
- `GET /api/customers/{id}/360`: Staff Customer 360 degree view.

---

## 7. Angular Component & Route Architecture

### Public Routes:
- `/rentals` & `/store/:tenantSlug`: Public product catalog storefront.
- `/rentals/:productId`: Public product detail page with date & availability check.
- `/rentals/cart`: Shopping cart view.
- `/rentals/request-quote`: Quote request form.
- `/rentals/checkout`: Multi-step checkout wizard.
- `/checkout/success`: Checkout confirmation page.
- `/quote-request/success`: Quote request confirmation page.

### Customer Portal Routes (`/portal` layout):
- `/portal/login`: Customer login page.
- `/portal/register`: Customer registration page.
- `/portal/dashboard`: Dashboard overview.
- `/portal/bookings` & `/portal/bookings/:id`: Bookings list & detail with rental status timeline.
- `/portal/quotes` & `/portal/quotes/:id`: Quotes list & detail with Approve/Decline actions.
- `/portal/invoices` & `/portal/invoices/:id`: Invoices list & detail.
- `/portal/payments`: Payment history list.
- `/portal/damage-claims` & `/portal/damage-claims/:id`: Customer damage claims management.
- `/portal/messages`: Threaded messages between customer and staff.
- `/portal/addresses`: Saved addresses management.
- `/portal/profile`: Profile settings.

### Staff Dashboard Routes:
- `/customer-requests`: Staff customer request dashboard.
- `/customers/:id`: Customer 360 degree staff view.

---

## 8. Verification & Testing Strategy
1. **Catalog & Availability Verification**: Test product listing, search, category filter, price sorting, and date availability calculation.
2. **Cart & Validation**: Test adding products, updating quantities, validating availability against backend reservations, and cart expiration.
3. **Quote Approval Workflow**: Verify quote creation -> customer portal login -> quote detail view -> quote approval -> booking creation -> inventory reservation.
4. **Quote Decline Workflow**: Verify customer decline -> reason recording -> sales notification -> quote status updated to `DECLINED`.
5. **Damage Claims Customer Review**: Test claim estimate presentation, customer approval, and dispute workflow.
6. **Threaded Messaging**: Test customer initiating a message from a booking, staff receiving request, and conversation thread updates.
7. **Security & Tenant Isolation**: Test cross-customer access prevention (Customer A cannot access Customer B's bookings/quotes/invoices/claims/addresses) and tenant boundary enforcement.

---

## 9. AI Preparation & Clean Interfaces
- `CustomerAssistantService`: Interface for future AI assistance in customer inquiry resolution.
- `ProductRecommendationService`: Interface for rule-based rental bundle recommendations (e.g. recommend tables & linens when chairs are added).
- `QuoteAssistantService`: Interface for automated quote generation assistance.
- `MessageAssistantService`: Interface for AI draft response generation for staff.

---

## 10. Summary
Day 20 transforms RentFlow AI into a complete, modern customer-facing SaaS product. Customers can browse, check live inventory availability, request quotes, complete bookings, manage payments, review damage claims, and communicate with rental staff, while strict multi-tenant isolation, RBAC controls, and existing workflows from Days 1–19 remain completely intact.
