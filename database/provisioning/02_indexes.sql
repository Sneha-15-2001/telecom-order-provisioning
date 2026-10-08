-- Provisioning Service indexes (Phase 5).

CREATE INDEX IF NOT EXISTS idx_prv_order ON provisioning_request (order_id);
CREATE INDEX IF NOT EXISTS idx_prv_status ON provisioning_request (status);
CREATE INDEX IF NOT EXISTS idx_prv_type ON provisioning_request (service_type);
CREATE INDEX IF NOT EXISTS idx_prv_history_request ON provisioning_history (request_id);
