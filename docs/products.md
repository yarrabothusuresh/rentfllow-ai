# Product Catalog Architecture & Multi-Tenant Inventory Model

## Overview
RentFlow AI's Product Catalog provides the asset foundation for party and event rental operations. Each rental product represents a physical or operational asset (e.g., Chiavari Chairs, Tents, Uplights, Linens) with financial attributes, inventory level breakdown, and date-based availability capabilities.

## Data Model & Schemas

### `ProductCategory`
Supports hierarchical parent-child category trees:
- `id`: UUID (Primary Key)
- `tenantId`: String (Multi-tenant partition)
- `name`: String
- `description`: String
- `parentCategoryId`: UUID (Self-referential parent category)
- `active`: Boolean

### `Product`
- `id`: UUID (Primary Key)
- `tenantId`: String (Strict multi-tenant isolation)
- `sku`: String (Unique item code, e.g., `CHI-001`)
- `name`: String (Product title)
- `description`: String
- `categoryId`: UUID
- `productType`: Enum (`RENTAL_ITEM`, `PACKAGE`, `SERVICE`, `CONSUMABLE`)
- `status`: Enum (`ACTIVE`, `INACTIVE`, `DRAFT`, `DISCONTINUED`)
- `rentalPrice`: `BigDecimal` (Monetary rental rate per day/event)
- `replacementCost`: `BigDecimal` (Asset replacement value — protected for Customer role)
- `quantityOwned`: `int` (Total fleet acquisition quantity)
- `quantityInMaintenance`: `int` (Items currently undergoing repair or servicing)
- `quantityDamaged`: `int` (Damaged items unavailable for rent)
- `quantityLost`: `int` (Lost or unreturned items)
- `imageUrl`: String

## Security & Field Protection Rule
To protect commercial sensitivity:
- When a user with the **`CUSTOMER`** role requests product details or product lists, the system programmatically redacts `replacementCost` by setting it to `null`.
- Direct API endpoints prevent `CUSTOMER` users from creating, updating, or deleting catalog products (HTTP 403 Forbidden).
