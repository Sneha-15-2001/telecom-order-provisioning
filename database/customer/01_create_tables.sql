-- ============================================================
-- Customer Service database: telecom_customer (Phase 2)
-- Tables: corporate_account, customer, customer_address, subscription
-- Conventions: PKs, FKs, UNIQUE, NOT NULL, CHECKs, created_at/updated_at.
-- JPA `ddl-auto: update` keeps this in sync; this script is the
-- source of truth for fresh environments and reviews.
-- ============================================================

CREATE TABLE IF NOT EXISTS corporate_account (
    id                  BIGSERIAL PRIMARY KEY,
    account_number      VARCHAR(20)  NOT NULL UNIQUE,
    company_name        VARCHAR(200) NOT NULL,
    registration_number VARCHAR(50)  NOT NULL UNIQUE,
    contact_email       VARCHAR(255) NOT NULL,
    contact_phone       VARCHAR(20)  NOT NULL,
    status              VARCHAR(20)  NOT NULL
        CHECK (status IN ('ACTIVE','INACTIVE','SUSPENDED','BLOCKED')),
    created_at          TIMESTAMP    NOT NULL DEFAULT NOW(),
    updated_at          TIMESTAMP    NOT NULL DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS customer (
    id                  BIGSERIAL PRIMARY KEY,
    customer_number     VARCHAR(20)  NOT NULL UNIQUE,
    first_name          VARCHAR(100) NOT NULL,
    last_name           VARCHAR(100) NOT NULL,
    email               VARCHAR(255) NOT NULL UNIQUE,
    phone               VARCHAR(20)  NOT NULL UNIQUE,
    date_of_birth       DATE,
    customer_type       VARCHAR(20)  NOT NULL
        CHECK (customer_type IN ('INDIVIDUAL','CORPORATE')),
    status              VARCHAR(20)  NOT NULL
        CHECK (status IN ('ACTIVE','INACTIVE','SUSPENDED','BLOCKED')),
    corporate_account_id BIGINT REFERENCES corporate_account (id),
    created_at          TIMESTAMP    NOT NULL DEFAULT NOW(),
    updated_at          TIMESTAMP    NOT NULL DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS customer_address (
    id              BIGSERIAL PRIMARY KEY,
    customer_id     BIGINT       NOT NULL REFERENCES customer (id),
    address_type    VARCHAR(20)  NOT NULL
        CHECK (address_type IN ('BILLING','SHIPPING','HOME','OFFICE')),
    street          VARCHAR(255) NOT NULL,
    city            VARCHAR(100) NOT NULL,
    state           VARCHAR(100),
    postal_code     VARCHAR(20)  NOT NULL,
    country         VARCHAR(100) NOT NULL,
    is_primary      BOOLEAN      NOT NULL DEFAULT FALSE,
    created_at      TIMESTAMP    NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMP    NOT NULL DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS subscription (
    id                  BIGSERIAL PRIMARY KEY,
    customer_id         BIGINT       NOT NULL REFERENCES customer (id),
    subscription_number VARCHAR(20)  NOT NULL UNIQUE,
    plan_code           VARCHAR(50)  NOT NULL,
    plan_name           VARCHAR(150) NOT NULL,
    msisdn              VARCHAR(15)  UNIQUE,
    status              VARCHAR(20)  NOT NULL
        CHECK (status IN ('PENDING','ACTIVE','SUSPENDED','CANCELLED')),
    start_date          DATE         NOT NULL,
    end_date            DATE,
    created_at          TIMESTAMP    NOT NULL DEFAULT NOW(),
    updated_at          TIMESTAMP    NOT NULL DEFAULT NOW()
);
