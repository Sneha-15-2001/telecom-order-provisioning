-- ============================================================
-- Inventory Service database: telecom_inventory (Phase 4)
-- Tables: inventory_resource, resource_reservation, inventory_history.
-- ============================================================

CREATE TABLE IF NOT EXISTS inventory_resource (
    id              BIGSERIAL PRIMARY KEY,
    resource_number VARCHAR(20)  NOT NULL UNIQUE,
    resource_type   VARCHAR(20)  NOT NULL
        CHECK (resource_type IN ('SIM','ESIM','MSISDN','DEVICE','FIBER_PORT','ONT','ROUTER','NETWORK_PROFILE')),
    identifier      VARCHAR(100) NOT NULL UNIQUE,
    status          VARCHAR(20)  NOT NULL
        CHECK (status IN ('AVAILABLE','RESERVED','ALLOCATED','BLOCKED','QUARANTINED','DECOMMISSIONED')),
    details         VARCHAR(1000),
    order_id        BIGINT,
    created_at      TIMESTAMP    NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMP    NOT NULL DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS resource_reservation (
    id                 BIGSERIAL PRIMARY KEY,
    reservation_number VARCHAR(20) NOT NULL UNIQUE,
    resource_id        BIGINT      NOT NULL REFERENCES inventory_resource (id),
    order_id           BIGINT      NOT NULL,
    customer_id        BIGINT,
    status             VARCHAR(20) NOT NULL
        CHECK (status IN ('ACTIVE','CONFIRMED','CANCELLED','EXPIRED')),
    reserved_at        TIMESTAMP   NOT NULL DEFAULT NOW(),
    expires_at         TIMESTAMP   NOT NULL,
    confirmed_at       TIMESTAMP
);

CREATE TABLE IF NOT EXISTS inventory_history (
    id          BIGSERIAL PRIMARY KEY,
    resource_id BIGINT       NOT NULL REFERENCES inventory_resource (id),
    event       VARCHAR(50)  NOT NULL,
    from_status VARCHAR(20),
    to_status   VARCHAR(20),
    comment     VARCHAR(1000),
    created_at  TIMESTAMP    NOT NULL DEFAULT NOW()
);
