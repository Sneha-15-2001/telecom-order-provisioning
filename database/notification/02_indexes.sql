-- Notification Service indexes (Phase 6).

CREATE INDEX IF NOT EXISTS idx_notification_status ON notification (status);
CREATE INDEX IF NOT EXISTS idx_notification_channel ON notification (channel);
CREATE INDEX IF NOT EXISTS idx_notification_order ON notification (order_id);
CREATE INDEX IF NOT EXISTS idx_notification_customer ON notification (customer_id);
CREATE INDEX IF NOT EXISTS idx_attempt_notification ON notification_attempt (notification_id);
