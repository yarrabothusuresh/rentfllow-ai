-- ====================================================================
-- RentFlow AI - V4__booking_checkout_idempotency.sql
-- Day 35: Booking, Checkout, Lead & Rental Request Idempotency
-- ====================================================================

-- 1. Rental Requests Idempotency & Tamper Protection
ALTER TABLE rental_requests
    ADD COLUMN IF NOT EXISTS idempotency_key VARCHAR(255);

ALTER TABLE rental_requests
    ADD COLUMN IF NOT EXISTS request_hash VARCHAR(64);

ALTER TABLE rental_requests
    ADD CONSTRAINT uq_rental_req_tenant_idempotency
    UNIQUE (tenant_id, idempotency_key);

CREATE INDEX IF NOT EXISTS idx_rental_req_tenant_idemp
    ON rental_requests (tenant_id, idempotency_key);

-- 2. CRM Lead Deduplication on Rental Request
ALTER TABLE crm_leads
    ADD CONSTRAINT uq_crm_lead_tenant_rental_req
    UNIQUE (tenant_id, rental_request_id);

-- 3. Booking Deduplication on Quote Conversion
ALTER TABLE booking
    ADD CONSTRAINT uq_booking_tenant_quote
    UNIQUE (tenant_id, quote_id);

-- 4. Quotes Idempotency Index
ALTER TABLE quotes
    ADD COLUMN IF NOT EXISTS idempotency_key VARCHAR(255);

CREATE INDEX IF NOT EXISTS idx_quotes_tenant_idempotency
    ON quotes (tenant_id, idempotency_key);
