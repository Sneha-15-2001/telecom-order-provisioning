-- ============================================================
-- Provisioning Service database: telecom_provisioning (Phase 5)
-- Tables: provisioning_request, provisioning_history, service_profile.
-- Simulation only — NEVER touches real network elements.
-- ============================================================

CREATE TABLE IF NOT EXISTS provisioning_request (
    id               BIGSERIAL PRIMARY KEY,
    request_number   VARCHAR(20) NOT NULL UNIQUE,
    order_id         BIGINT      NOT NULL,
    customer_id      BIGINT,
    service_type     VARCHAR(20) NOT NULL
        CHECK (service_type IN ('MOBILE','ESIM','BROADBAND','FIBER','ROAMING','DEVICE')),
    msisdn           VARCHAR(15),
    resource_number  VARCHAR(20),
    plan_code        VARCHAR(50),
    status           VARCHAR(20) NOT NULL
        CHECK (status IN ('PENDING','IN_PROGRESS','COMPLETED','FAILED','CANCELLED','ROLLED_BACK')),
    attempts         INTEGER     NOT NULL DEFAULT 0,
    last_error       VARCHAR(1000),
    created_at       TIMESTAMP   NOT NULL DEFAULT NOW(),
    updated_at       TIMESTAMP   NOT NULL DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS provisioning_history (
    id          BIGSERIAL PRIMARY KEY,
    request_id  BIGINT       NOT NULL REFERENCES provisioning_request (id),
    event       VARCHAR(50)  NOT NULL,
    from_status VARCHAR(20),
    to_status   VARCHAR(20),
    comment     VARCHAR(1000),
    created_at  TIMESTAMP    NOT NULL DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS service_profile (
    id           BIGSERIAL PRIMARY KEY,
    profile_code VARCHAR(50)  NOT NULL UNIQUE,
    service_type VARCHAR(20)  NOT NULL
        CHECK (service_type IN ('MOBILE','ESIM','BROADBAND','FIBER','ROAMING','DEVICE')),
    description  VARCHAR(500) NOT NULL,
    config       VARCHAR(2000),
    active       BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at   TIMESTAMP    NOT NULL DEFAULT NOW(),
    updated_at   TIMESTAMP    NOT NULL DEFAULT NOW()
);
