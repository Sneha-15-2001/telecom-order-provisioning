-- Customer Service indexes (Phase 2).
-- Unique constraints are already created inline in 01_create_tables.sql.

CREATE INDEX IF NOT EXISTS idx_customer_status ON customer (status);
CREATE INDEX IF NOT EXISTS idx_customer_type ON customer (customer_type);
CREATE INDEX IF NOT EXISTS idx_customer_last_name ON customer (last_name);
CREATE INDEX IF NOT EXISTS idx_customer_corporate ON customer (corporate_account_id);
CREATE INDEX IF NOT EXISTS idx_address_customer ON customer_address (customer_id);
CREATE INDEX IF NOT EXISTS idx_subscription_customer ON subscription (customer_id);
CREATE INDEX IF NOT EXISTS idx_subscription_status ON subscription (status);
CREATE INDEX IF NOT EXISTS idx_subscription_plan ON subscription (plan_code);
