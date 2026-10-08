-- ============================================================
-- Order Service database: telecom_order (Phase 3)
-- Tables: telecom_order (ORDER is a reserved word), order_item,
-- order_history, payment, promotion.
-- Customer identity is stored as plain IDs (no cross-service FKs);
-- cross-service reads go through REST from Phase 7.
-- ============================================================

CREATE TABLE IF NOT EXISTS telecom_order (
    id              BIGSERIAL PRIMARY KEY,
    order_number    VARCHAR(20)  NOT NULL UNIQUE,
    customer_id     BIGINT       NOT NULL,
    customer_number VARCHAR(20),
    order_type      VARCHAR(20)  NOT NULL
        CHECK (order_type IN ('NEW_CONNECTION','UPGRADE','PLAN_CHANGE','DEVICE_ONLY','BROADBAND','BULK')),
    status          VARCHAR(20)  NOT NULL
        CHECK (status IN ('CREATED','VALIDATING','VALIDATED','PAYMENT_PENDING','PAYMENT_COMPLETED',
                          'INVENTORY_RESERVED','PROVISIONING','ACTIVATING','COMPLETED','FAILED',
                          'CANCELLED','ROLLING_BACK','RETRYING')),
    priority        VARCHAR(20)  NOT NULL DEFAULT 'NORMAL'
        CHECK (priority IN ('NORMAL','HIGH','URGENT')),
    total_amount    NUMERIC(12,2) NOT NULL DEFAULT 0,
    discount_amount NUMERIC(12,2) NOT NULL DEFAULT 0,
    promo_code      VARCHAR(50),
    notes           VARCHAR(1000),
    created_at      TIMESTAMP    NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMP    NOT NULL DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS order_item (
    id           BIGSERIAL PRIMARY KEY,
    order_id     BIGINT       NOT NULL REFERENCES telecom_order (id),
    item_type    VARCHAR(20)  NOT NULL
        CHECK (item_type IN ('MOBILE_PLAN','DEVICE','SIM','ESIM','BROADBAND','FIBER_EQUIPMENT','ROAMING_PACK','ADDON')),
    product_code VARCHAR(50)  NOT NULL,
    product_name VARCHAR(150) NOT NULL,
    quantity     INTEGER      NOT NULL CHECK (quantity >= 1),
    unit_price   NUMERIC(12,2) NOT NULL CHECK (unit_price >= 0),
    msisdn       VARCHAR(15),
    created_at   TIMESTAMP    NOT NULL DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS order_history (
    id          BIGSERIAL PRIMARY KEY,
    order_id    BIGINT       NOT NULL REFERENCES telecom_order (id),
    event       VARCHAR(50)  NOT NULL,
    from_status VARCHAR(20),
    to_status   VARCHAR(20),
    comment     VARCHAR(1000),
    created_at  TIMESTAMP    NOT NULL DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS payment (
    id                BIGSERIAL PRIMARY KEY,
    order_id          BIGINT       NOT NULL REFERENCES telecom_order (id),
    payment_reference VARCHAR(40)  NOT NULL UNIQUE,
    amount            NUMERIC(12,2) NOT NULL CHECK (amount > 0),
    method            VARCHAR(20)  NOT NULL
        CHECK (method IN ('CARD','UPI','NETBANKING','WALLET','CASH')),
    status            VARCHAR(20)  NOT NULL
        CHECK (status IN ('PENDING','SUCCESS','FAILED','REFUNDED')),
    created_at        TIMESTAMP    NOT NULL DEFAULT NOW(),
    updated_at        TIMESTAMP    NOT NULL DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS promotion (
    id             BIGSERIAL PRIMARY KEY,
    promo_code     VARCHAR(50)  NOT NULL UNIQUE,
    description    VARCHAR(500) NOT NULL,
    discount_type  VARCHAR(20)  NOT NULL CHECK (discount_type IN ('PERCENTAGE','FLAT')),
    discount_value NUMERIC(12,2) NOT NULL CHECK (discount_value > 0),
    active         BOOLEAN      NOT NULL DEFAULT TRUE,
    valid_from     DATE,
    valid_to       DATE,
    created_at     TIMESTAMP    NOT NULL DEFAULT NOW(),
    updated_at     TIMESTAMP    NOT NULL DEFAULT NOW()
);
