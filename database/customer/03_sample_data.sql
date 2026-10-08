-- Customer Service sample data (Phase 2). Idempotent via ON CONFLICT.

INSERT INTO corporate_account
    (account_number, company_name, registration_number, contact_email, contact_phone, status)
VALUES
    ('CORP-DEMO01', 'Acme Telecom Pvt Ltd', 'CIN-U64200KA2015PTC000001', 'telecom@acme.example', '+918012345678', 'ACTIVE')
ON CONFLICT (account_number) DO NOTHING;

INSERT INTO customer
    (customer_number, first_name, last_name, email, phone, date_of_birth, customer_type, status, corporate_account_id)
VALUES
    ('CUS-DEMO001', 'Aarav', 'Sharma', 'aarav.sharma@example.com', '+919876543210', '1995-04-17', 'INDIVIDUAL', 'ACTIVE', NULL),
    ('CUS-DEMO002', 'Priya', 'Nair', 'priya.nair@example.com', '+919876543211', '1990-11-02', 'INDIVIDUAL', 'ACTIVE', NULL),
    ('CUS-DEMO003', 'Rohan', 'Iyer', 'rohan.iyer@example.com', '+919876543212', '1988-06-25', 'CORPORATE', 'SUSPENDED',
        (SELECT id FROM corporate_account WHERE account_number = 'CORP-DEMO01'))
ON CONFLICT (customer_number) DO NOTHING;

INSERT INTO customer_address
    (customer_id, address_type, street, city, state, postal_code, country, is_primary)
SELECT id, 'HOME', '221 MG Road', 'Bengaluru', 'Karnataka', '560001', 'India', TRUE
FROM customer WHERE customer_number = 'CUS-DEMO001'
ON CONFLICT DO NOTHING;

INSERT INTO subscription
    (customer_id, subscription_number, plan_code, plan_name, msisdn, status, start_date)
SELECT id, 'SUB-DEMO001', 'PLAN_5G_299', '5G Unlimited 299', '919876543210', 'ACTIVE', CURRENT_DATE
FROM customer WHERE customer_number = 'CUS-DEMO001'
ON CONFLICT (subscription_number) DO NOTHING;
