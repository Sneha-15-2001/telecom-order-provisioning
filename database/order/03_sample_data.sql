-- Order Service sample data (Phase 3). Idempotent via ON CONFLICT.

INSERT INTO promotion (promo_code, description, discount_type, discount_value, active, valid_from, valid_to)
VALUES
    ('FESTIVE10', 'Festive season 10% off', 'PERCENTAGE', 10, TRUE, CURRENT_DATE - 30, CURRENT_DATE + 60),
    ('FLAT50', 'Flat 50 off broadband orders', 'FLAT', 50, TRUE, CURRENT_DATE - 30, CURRENT_DATE + 60),
    ('EXPIRED5', 'Expired 5% promo (for validity tests)', 'PERCENTAGE', 5, TRUE, CURRENT_DATE - 90, CURRENT_DATE - 30)
ON CONFLICT (promo_code) DO NOTHING;

-- Demo order in VALIDATED state (matches customer CUS-DEMO001 from customer DB).
INSERT INTO telecom_order
    (order_number, customer_id, customer_number, order_type, status, priority, total_amount, discount_amount, notes)
VALUES
    ('ORD-DEMO001', 1, 'CUS-DEMO001', 'NEW_CONNECTION', 'VALIDATED', 'NORMAL', 299.00, 0, 'Demo order for Phase 3 verification')
ON CONFLICT (order_number) DO NOTHING;

INSERT INTO order_item (order_id, item_type, product_code, product_name, quantity, unit_price, msisdn)
SELECT id, 'MOBILE_PLAN', 'PLAN_5G_299', '5G Unlimited 299', 1, 299.00, '919876543210'
FROM telecom_order WHERE order_number = 'ORD-DEMO001'
ON CONFLICT DO NOTHING;

INSERT INTO order_history (order_id, event, from_status, to_status, comment)
SELECT id, 'ORDER_CREATED', NULL, 'CREATED', '1 item(s), type NEW_CONNECTION'
FROM telecom_order WHERE order_number = 'ORD-DEMO001'
ON CONFLICT DO NOTHING;

INSERT INTO order_history (order_id, event, from_status, to_status, comment)
SELECT id, 'STATUS_CHANGED', 'CREATED', 'VALIDATED', 'all local checks passed'
FROM telecom_order WHERE order_number = 'ORD-DEMO001'
ON CONFLICT DO NOTHING;

-- ---------------------------------------------------------------------------
-- Sellable catalogue. Shaped like a real operator's price list: prepaid plans
-- with data allowances, postpaid plans, devices on EMI-free monthly terms,
-- SIM variants, and broadband tiers with different speeds.
-- ---------------------------------------------------------------------------
INSERT INTO product (product_code, name, item_type, price, description, data_gb) VALUES
    ('PLAN_PREPAID_199',   'Prepaid PayGo 199',        'MOBILE_PLAN', 199.00,  '28-day validity, unlimited calls, 2GB/day data', 60),
    ('PLAN_PREPAID_299',   'Prepaid Unlimited 299',    'MOBILE_PLAN', 299.00,  '28-day validity, unlimited data and calls',       999),
    ('PLAN_PREPAID_599',   'Prepaid Unlimited Plus',   'MOBILE_PLAN', 599.00,  '56-day validity, 5G, unlimited data',            999),
    ('PLAN_POSTPAID_499',  'Postpaid Value 499',       'MOBILE_PLAN', 499.00,  'Unlimited local and STD, 100GB/month',            100),
    ('PLAN_POSTPAID_799',  'Postpaid Unlimited 799',   'MOBILE_PLAN', 799.00,  'Unlimited national roaming, 200GB/month',        200),
    ('PLAN_POSTPAID_1099', 'Postpaid Family Share',    'MOBILE_PLAN', 1099.00, 'Primary plus 4 secondary SIMs, 300GB shared',     300),
    ('PLAN_5G_299',        '5G Unlimited 299',         'MOBILE_PLAN', 299.00,  '5G data, unlimited calls',                       100),
    ('DEVICE_XIAOMI_13',   'Xiaomi 13 (128GB)',        'DEVICE',      24999.00,'5G, 128GB storage',                              NULL),
    ('DEVICE_SAMUNG_A54',  'Samsung Galaxy A54',       'DEVICE',      33999.00,'5G, 128GB storage',                              NULL),
    ('DEVICE_IPHONE_15',   'iPhone 15 (128GB)',        'DEVICE',      79900.00,'5G, 128GB storage',                              NULL),
    ('SIM_STANDARD',       'Standard SIM',             'SIM',         199.00,  'Physical SIM with a starter bundle',             NULL),
    ('SIM_DUAL',           'Dual-SIM Starter',         'SIM',         299.00,  'Two SIMs with a combined data bundle',           NULL),
    ('ESIM_LITE',          'eSIM Data-only',           'ESIM',        149.00,  'Data-only eSIM, no voice',                        50),
    ('BB_FIBER_100',       'Fiber 100 Mbps',           'BROADBAND',   999.00,  '100 Mbps unlimited, router included',             NULL),
    ('BB_FIBER_300',       'Fiber 300 Mbps',           'BROADBAND',  1499.00,  '300 Mbps unlimited, static IP add-on available',  NULL),
    ('BB_FIBER_GIGA',      'Fiber Giga 1 Gbps',        'BROADBAND',  2499.00,  '1 Gbps unlimited, business-grade SLA',            NULL),
    ('ONT_GIGA_ROUTER',    'Giga ONT + Wi-Fi 6 router','FIBER_EQUIPMENT', 1999.00, 'ONT and router for gigabit plans',            NULL),
    ('ADDON_DATA_10GB',    'Data top-up 10GB',         'ADDON',      149.00,   '10GB add-on, valid 30 days',                      10),
    ('ADDON_ROAM_INTL',    'International roaming 10 days','ROAMING_PACK', 999.00,'10 days roaming in 20 countries',          NULL)
ON CONFLICT (product_code) DO NOTHING;
