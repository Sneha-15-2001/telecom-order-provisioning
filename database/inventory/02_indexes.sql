-- Inventory Service indexes (Phase 4).

CREATE INDEX IF NOT EXISTS idx_resource_type ON inventory_resource (resource_type);
CREATE INDEX IF NOT EXISTS idx_resource_status ON inventory_resource (status);
CREATE INDEX IF NOT EXISTS idx_resource_order ON inventory_resource (order_id);
CREATE INDEX IF NOT EXISTS idx_reservation_resource ON resource_reservation (resource_id);
CREATE INDEX IF NOT EXISTS idx_reservation_order ON resource_reservation (order_id);
CREATE INDEX IF NOT EXISTS idx_reservation_status ON resource_reservation (status);
CREATE INDEX IF NOT EXISTS idx_inv_history_resource ON inventory_history (resource_id);
