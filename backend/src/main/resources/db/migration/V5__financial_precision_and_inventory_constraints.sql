-- ====================================================================
-- RentFlow AI - V5__financial_precision_and_inventory_constraints.sql
-- Day 36 & 37: Financial Accuracy Invariants & Inventory Concurrency Constraints
-- ====================================================================

-- 1. Inventory Warehouse Stock Uniqueness Constraint
-- Guarantees atomic stock allocation without duplicate per-warehouse ledger rows
ALTER TABLE warehouse_stock 
    ADD CONSTRAINT uk_wh_stock_product_warehouse 
    UNIQUE (tenant_id, product_id, warehouse_id);

-- 2. Physical Inventory Quantity Non-Negative Check Invariant
-- Ensures physical stock count cannot become negative due to corrupt updates
ALTER TABLE products 
    ADD CONSTRAINT chk_products_quantity_owned_nonneg 
    CHECK (quantity_owned >= 0);

-- 3. Date-Interval Availability Overlap Composite Lookup Index
-- Accelerates time-windowed reservation conflict lookups under row-level product locks
CREATE INDEX IF NOT EXISTS idx_inv_res_product_dates 
    ON inventory_reservations (tenant_id, product_id, start_date_time, end_date_time);
