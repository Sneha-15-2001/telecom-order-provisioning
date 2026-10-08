-- Order Service indexes (Phase 3).
-- Unique constraints are already created inline in 01_create_tables.sql.

CREATE INDEX IF NOT EXISTS idx_order_customer ON telecom_order (customer_id);
CREATE INDEX IF NOT EXISTS idx_order_status ON telecom_order (status);
CREATE INDEX IF NOT EXISTS idx_order_type ON telecom_order (order_type);
CREATE INDEX IF NOT EXISTS idx_order_customer_number ON telecom_order (customer_number);
CREATE INDEX IF NOT EXISTS idx_item_order ON order_item (order_id);
CREATE INDEX IF NOT EXISTS idx_item_product ON order_item (product_code);
CREATE INDEX IF NOT EXISTS idx_history_order ON order_history (order_id);
CREATE INDEX IF NOT EXISTS idx_payment_order ON payment (order_id);
CREATE INDEX IF NOT EXISTS idx_payment_status ON payment (status);
CREATE INDEX IF NOT EXISTS idx_promo_active ON promotion (active);
