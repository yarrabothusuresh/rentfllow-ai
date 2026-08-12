# User Roles & Permissions Architecture

This document details the tenant isolation, user entities, authorization hierarchy, and permission sets implemented in **RentFlow AI**.

---

## 1. Tenant/Organization Model
RentFlow AI is structured as a multi-tenant SaaS application:
* **Tenant (Organization)**: A rental business entity (e.g., *Evergreen Event Rentals* in Dallas, TX).
* **Users (Employees)**: Multiple users belong to a single tenant. Users receive role-based permissions scoped to their tenant's boundary.
* **Customers**: External clients who interact with storefronts, quotes, and payment pages. Their access is isolated to their specific customer records.

---

## 2. Relational Entity Schema
The database model uses a standard Role-Based Access Control (RBAC) schema mapping:

```
Tenant (UUID)
  │
  └── User (UUID)
       │
       └── UserRole (Join table)
              │
              └── Role (UUID)
                    │
                    └── RolePermission (Join table)
                           │
                           └── Permission (UUID)
```

### Table Definitions
1. `tenant`: ID, Name.
2. `app_user`: ID, Name, Tenant ID (foreign key).
3. `role`: ID, RoleType (Enum).
4. `permission`: ID, PermissionCode (Enum), Description.
5. `user_role`: Join table mapping user IDs to role IDs.
6. `role_permission`: Join table mapping role IDs to permission IDs.

---

## 3. Supported Roles & Responsibilities

| Role | RoleType Code | Primary Responsibility |
| :--- | :--- | :--- |
| **Owner** | `OWNER` | Full business configurations, financial overview, user administration, and AI insights. |
| **Admin** | `ADMIN` | Manage standard operations, catalog data, bookings, schedules, and standard user profiles. |
| **Sales** | `SALES` | Respond to inbound leads, construct pricing quotes, manage pipelines, and negotiate deals. |
| **Warehouse** | `WAREHOUSE` | Manage product locations, stock numbers, order picking, cargo packing, and equipment returns. |
| **Driver** | `DRIVER` | Transport equipment to/from event venues, report setup completion, and collect pickup receipts. |
| **Customer** | `CUSTOMER` | External clients reviewing details, signing agreements, and paying deposit fees. |

---

## 4. Permission Code Definitions
Granular permissions allow modular access control without hardcoding check values:

* **Dashboard Access**: `DASHBOARD_VIEW`
* **AI Tooling**: `AI_COPILOT_USE`
* **Customer Registry**: `CUSTOMER_VIEW`, `CUSTOMER_CREATE`, `CUSTOMER_UPDATE`
* **Leads Pipeline**: `LEAD_VIEW`, `LEAD_CREATE`, `LEAD_UPDATE`
* **Product Catalog**: `PRODUCT_VIEW`, `PRODUCT_CREATE`, `PRODUCT_UPDATE`, `PRODUCT_DELETE`
* **Inventory Control**: `INVENTORY_VIEW`, `INVENTORY_UPDATE`, `INVENTORY_RESERVE`
* **Proposal Quotes**: `QUOTE_VIEW`, `QUOTE_CREATE`, `QUOTE_UPDATE`, `QUOTE_SEND`
* **Bookings Ledger**: `BOOKING_VIEW`, `BOOKING_CREATE`, `BOOKING_UPDATE`, `BOOKING_CANCEL`
* **Invoices & Payments**: `PAYMENT_VIEW`, `PAYMENT_CREATE`, `PAYMENT_REFUND`
* **Warehouse Layouts**: `WAREHOUSE_VIEW`, `WAREHOUSE_UPDATE`
* **Logistics & Dispatch**: `DELIVERY_VIEW`, `DELIVERY_UPDATE`
* **Financial Reporting**: `ANALYTICS_VIEW`
* **Public Catalog**: `STOREFRONT_VIEW`, `STOREFRONT_UPDATE`
* **Staff Management**: `USER_VIEW`, `USER_CREATE`, `USER_UPDATE`, `USER_DISABLE`
* **Tenant Configuration**: `COMPANY_SETTINGS_VIEW`, `COMPANY_SETTINGS_UPDATE`

---

## 5. Future Custom Roles Extensibility
The relational schema natively supports custom roles. If a business needs to introduce a custom role like `Event Coordinator`:
1. Create a new `role` record (e.g. name = `Event Coordinator`).
2. Insert mappings in `role_permission` for target codes (e.g. `LEAD_VIEW`, `CUSTOMER_VIEW`, `QUOTE_CREATE`, `BOOKING_VIEW`).
3. Link users to the new role in `user_role`.
No backend or frontend code changes are needed to define new access boundaries.
