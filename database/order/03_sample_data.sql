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
