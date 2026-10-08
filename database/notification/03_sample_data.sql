-- Notification Service sample data (Phase 6). Idempotent via ON CONFLICT.

INSERT INTO notification_template (template_code, channel, subject_template, body_template, active)
VALUES
    ('ORDER_CREATED', 'SMS', NULL, 'Hi, your order {{orderNumber}} ({{planName}}) is {{status}}. - Telecom', TRUE),
    ('ORDER_COMPLETED', 'SMS', NULL, 'Great news! Order {{orderNumber}} is complete. {{msisdn}} is active. - Telecom', TRUE),
    ('PROVISIONING_FAILED', 'SMS', NULL, 'We hit an issue activating order {{orderNumber}} ({{error}}). Our team is on it. - Telecom', TRUE),
    ('PAYMENT_RECEIVED', 'EMAIL', 'Payment received for {{orderNumber}}', 'We received {{amount}} for order {{orderNumber}}. Thank you! - Telecom', TRUE)
ON CONFLICT (template_code) DO NOTHING;
