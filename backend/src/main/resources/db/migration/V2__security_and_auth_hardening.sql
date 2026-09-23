-- ====================================================================
-- RentFlow AI - V2__security_and_auth_hardening.sql
-- Day 31-33: Multi-Tenant Security, JWT Auth & User Boundary Indexes
-- ====================================================================

-- 1. Multi-tenant composite lookup indexes for application staff users
CREATE INDEX IF NOT EXISTS idx_app_user_tenant_email 
    ON app_user (tenant_id, email);

-- 2. Customer Portal user lookup and isolation indexes
CREATE INDEX IF NOT EXISTS idx_customer_users_email_tenant 
    ON customer_users (tenant_id, email);

CREATE INDEX IF NOT EXISTS idx_customer_users_tenant_cust 
    ON customer_users (tenant_id, customer_id);

-- 3. External API Key tenant authentication index
CREATE INDEX IF NOT EXISTS idx_ext_api_key_tenant_status 
    ON external_api_key (tenant_id, status);
