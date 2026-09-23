-- ====================================================================
-- RentFlow AI - V3__payment_idempotency_constraints.sql
-- Day 34: Payment Idempotency & Financial Transaction Integrity
-- ====================================================================

-- 1. Authoritative Unique Constraint on Tenant-Scoped Transaction Reference
-- Guarantees at the storage engine layer that a single payment event affects RentFlow records exactly once
ALTER TABLE payment 
    ADD CONSTRAINT uq_payment_tenant_transaction_reference 
    UNIQUE (tenant_id, transaction_reference);

-- 2. Performance and Lock-Ordering Composite Indexes for Payment Lookups
CREATE INDEX IF NOT EXISTS idx_payment_tenant_ref 
    ON payment (tenant_id, transaction_reference);

CREATE INDEX IF NOT EXISTS idx_payment_invoice 
    ON payment (tenant_id, invoice_id);

CREATE INDEX IF NOT EXISTS idx_payment_tenant_booking 
    ON payment (tenant_id, booking_id);
