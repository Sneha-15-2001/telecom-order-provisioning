-- Inventory Service sample data (Phase 4). Idempotent via ON CONFLICT.

INSERT INTO inventory_resource (resource_number, resource_type, identifier, status, details)
VALUES
    ('RES-SIM0001', 'SIM', '899100000000000001', 'AVAILABLE', 'Physical SIM, profile A'),
    ('RES-SIM0002', 'SIM', '899100000000000002', 'AVAILABLE', 'Physical SIM, profile A'),
    ('RES-ESIM001', 'ESIM', '89012345678901234567890123456701', 'AVAILABLE', 'eSIM QR profile'),
    ('RES-MSISDN01', 'MSISDN', '919876543210', 'AVAILABLE', 'Bengaluru series number'),
    ('RES-MSISDN02', 'MSISDN', '919876543211', 'AVAILABLE', 'Bengaluru series number'),
    ('RES-DEV0001', 'DEVICE', '350000000000001', 'AVAILABLE', 'IMEI Pixel X8 128GB'),
    ('RES-FIBER01', 'FIBER_PORT', 'OLT1-PON3-PORT07', 'AVAILABLE', 'Koramangala OLT port'),
    ('RES-ONT0001', 'ONT', 'ONT-SN-ALCL001', 'AVAILABLE', 'ONT router combo'),
    ('RES-RTR0001', 'ROUTER', 'RTR-SN-0001', 'QUARANTINED', 'QA hold batch 42'),
    ('RES-NET0001', 'NETWORK_PROFILE', 'DEFAULT-5G-PROFILE', 'AVAILABLE', 'Default 5G service profile')
ON CONFLICT (resource_number) DO NOTHING;

INSERT INTO inventory_history (resource_id, event, from_status, to_status, comment)
SELECT id, 'RESOURCE_REGISTERED', NULL, 'AVAILABLE', 'seed data'
FROM inventory_resource WHERE resource_number = 'RES-SIM0001'
ON CONFLICT DO NOTHING;
