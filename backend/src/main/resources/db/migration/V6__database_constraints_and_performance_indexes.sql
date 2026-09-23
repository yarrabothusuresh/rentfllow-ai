-- ====================================================================
-- RentFlow AI - V6__database_constraints_and_performance_indexes.sql
-- Day 39: Database Constraints, Index Optimization & Tenant-Scoped Scalability
-- ====================================================================

-- --------------------------------------------------------------------
-- 1. Tenant-Scoped Uniqueness Constraints
-- --------------------------------------------------------------------
-- Replaces legacy global unique constraints with tenant-scoped uniqueness
-- so different tenants can independently sequence numbers (e.g. BKG-000001)

ALTER TABLE booking DROP CONSTRAINT IF EXISTS UK_6j74n7w8mp19sixr5272028mk;
ALTER TABLE quotes DROP CONSTRAINT IF EXISTS UK_bkbvhxprsi25u20qvjyt5aoay;
ALTER TABLE customers DROP CONSTRAINT IF EXISTS UK_t74y58jagthxqxysuw9l0jx6y;

ALTER TABLE booking
    ADD CONSTRAINT uq_booking_tenant_number
    UNIQUE (tenant_id, booking_number);

ALTER TABLE quotes
    ADD CONSTRAINT uq_quotes_tenant_number
    UNIQUE (tenant_id, quote_number);

ALTER TABLE customers
    ADD CONSTRAINT uq_customers_tenant_number
    UNIQUE (tenant_id, customer_number);

ALTER TABLE products
    ADD CONSTRAINT uq_products_tenant_sku
    UNIQUE (tenant_id, sku);

ALTER TABLE invoices
    ADD CONSTRAINT uq_invoices_tenant_number
    UNIQUE (tenant_id, invoice_number);

-- --------------------------------------------------------------------
-- 2. Redundant Index Removal
-- --------------------------------------------------------------------
-- Drop duplicate B-tree indexes that mirror existing UNIQUE constraints
DROP INDEX IF EXISTS idx_payment_tenant_ref;
DROP INDEX IF EXISTS idx_rental_req_tenant_idemp;

-- --------------------------------------------------------------------
-- 3. Database-Level CHECK Constraints (Critical Invariants)
-- --------------------------------------------------------------------
-- Rental Date Ordering (End >= Start)
ALTER TABLE booking
    ADD CONSTRAINT chk_booking_rental_dates
    CHECK (rental_end_date_time >= rental_start_date_time);

ALTER TABLE quotes
    ADD CONSTRAINT chk_quotes_rental_dates
    CHECK (rental_end_date_time >= rental_start_date_time);

-- Positive Quantities & Non-negative Prices on Line Items
ALTER TABLE booking_item
    ADD CONSTRAINT chk_booking_item_quantity
    CHECK (quantity > 0);

ALTER TABLE booking_item
    ADD CONSTRAINT chk_booking_item_unit_price
    CHECK (unit_price >= 0);

ALTER TABLE quote_items
    ADD CONSTRAINT chk_quote_items_quantity
    CHECK (quantity > 0);

ALTER TABLE quote_items
    ADD CONSTRAINT chk_quote_items_unit_price
    CHECK (unit_price >= 0);

-- Positive Payment Amounts
ALTER TABLE payment
    ADD CONSTRAINT chk_payment_amount_positive
    CHECK (amount > 0);

-- Product Price & Quantity Invariants
ALTER TABLE products
    ADD CONSTRAINT chk_products_rental_price
    CHECK (rental_price IS NULL OR rental_price >= 0);

ALTER TABLE products
    ADD CONSTRAINT chk_products_replacement_cost
    CHECK (replacement_cost IS NULL OR replacement_cost >= 0);

ALTER TABLE products
    ADD CONSTRAINT chk_products_quantities_nonneg
    CHECK (quantity_damaged >= 0 AND quantity_in_maintenance >= 0 AND quantity_lost >= 0);

-- Invoice Non-negative Financial Balances
ALTER TABLE invoices
    ADD CONSTRAINT chk_invoices_amounts_nonneg
    CHECK (total_amount >= 0 AND amount_paid >= 0 AND balance_due >= 0);

-- --------------------------------------------------------------------
-- 4. Foreign Key Constraints (Line Item Integrity)
-- --------------------------------------------------------------------
ALTER TABLE booking_item
    ADD CONSTRAINT fk_booking_item_booking
    FOREIGN KEY (booking_id) REFERENCES booking(id) ON DELETE CASCADE;

ALTER TABLE quote_items
    ADD CONSTRAINT fk_quote_items_quote
    FOREIGN KEY (quote_id) REFERENCES quotes(id) ON DELETE CASCADE;

ALTER TABLE invoice_items
    ADD CONSTRAINT fk_invoice_items_invoice
    FOREIGN KEY (invoice_id) REFERENCES invoices(id) ON DELETE CASCADE;

ALTER TABLE inventory_reservations
    ADD CONSTRAINT fk_inv_res_product
    FOREIGN KEY (product_id) REFERENCES products(id) ON DELETE RESTRICT;

-- --------------------------------------------------------------------
-- 5. Tenant-Scoped Composite Performance Indexes
-- --------------------------------------------------------------------
-- Booking listing, filtering and portal queries
CREATE INDEX IF NOT EXISTS idx_booking_tenant_created
    ON booking (tenant_id, created_at DESC);

CREATE INDEX IF NOT EXISTS idx_booking_tenant_status_created
    ON booking (tenant_id, status, created_at DESC);

CREATE INDEX IF NOT EXISTS idx_booking_tenant_customer_created
    ON booking (tenant_id, customer_id, created_at DESC);

-- Customer listing and lookup queries
CREATE INDEX IF NOT EXISTS idx_customers_tenant_created
    ON customers (tenant_id, created_at DESC);

CREATE INDEX IF NOT EXISTS idx_customers_tenant_name
    ON customers (tenant_id, last_name, first_name);

-- Product catalog listing and category filtering
CREATE INDEX IF NOT EXISTS idx_products_tenant_created
    ON products (tenant_id, created_at DESC);

CREATE INDEX IF NOT EXISTS idx_products_tenant_status
    ON products (tenant_id, status);

CREATE INDEX IF NOT EXISTS idx_products_tenant_category
    ON products (tenant_id, category_id);

-- Quote listing, filtering and portal queries
CREATE INDEX IF NOT EXISTS idx_quotes_tenant_created
    ON quotes (tenant_id, created_at DESC);

CREATE INDEX IF NOT EXISTS idx_quotes_tenant_status_created
    ON quotes (tenant_id, status, created_at DESC);

CREATE INDEX IF NOT EXISTS idx_quotes_tenant_customer_created
    ON quotes (tenant_id, customer_id, created_at DESC);

-- Invoice listing, status, due date and customer lookups
CREATE INDEX IF NOT EXISTS idx_invoices_tenant_created
    ON invoices (tenant_id, created_at DESC);

CREATE INDEX IF NOT EXISTS idx_invoices_tenant_status_due
    ON invoices (tenant_id, status, due_date);

CREATE INDEX IF NOT EXISTS idx_invoices_tenant_customer_created
    ON invoices (tenant_id, customer_id, created_at DESC);

-- Payment listing and customer queries
CREATE INDEX IF NOT EXISTS idx_payment_tenant_created
    ON payment (tenant_id, created_at DESC);

CREATE INDEX IF NOT EXISTS idx_payment_tenant_customer
    ON payment (tenant_id, customer_id, created_at DESC);
