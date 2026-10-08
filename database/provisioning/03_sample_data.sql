-- Provisioning Service sample data (Phase 5). Idempotent via ON CONFLICT.

INSERT INTO service_profile (profile_code, service_type, description, config, active)
VALUES
    ('PROFILE-5G-DEFAULT', 'MOBILE', 'Default 5G standalone profile', '{"slice":"embb","qos":"gold"}', TRUE),
    ('PROFILE-FIBER-200M', 'BROADBAND', 'Fiber 200Mbps residential profile', '{"downlink":"200M","uplink":"50M"}', TRUE),
    ('PROFILE-ESIM-QR', 'ESIM', 'eSIM QR onboarding profile', '{"format":"qr","validityDays":30}', TRUE)
ON CONFLICT (profile_code) DO NOTHING;

-- Demo request: MOBILE activation for order 2 (matches order-service demo data).
INSERT INTO provisioning_request
    (request_number, order_id, customer_id, service_type, msisdn, plan_code, status, attempts)
VALUES
    ('PRV-DEMO001', 2, 1, 'MOBILE', '919876543210', 'PLAN_5G_299', 'COMPLETED', 1)
ON CONFLICT (request_number) DO NOTHING;

INSERT INTO provisioning_history (request_id, event, from_status, to_status, comment)
SELECT id, 'PROVISIONING_CREATED', NULL, 'PENDING', 'service MOBILE for order 2'
FROM provisioning_request WHERE request_number = 'PRV-DEMO001'
ON CONFLICT DO NOTHING;
