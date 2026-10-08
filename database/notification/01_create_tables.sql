-- ============================================================
-- Notification Service database: telecom_notification (Phase 6)
-- Tables: notification, notification_template, notification_attempt.
-- Simulated SMS/email only — NO real provider integration.
-- ============================================================

CREATE TABLE IF NOT EXISTS notification (
    id                  BIGSERIAL PRIMARY KEY,
    notification_number VARCHAR(20)  NOT NULL UNIQUE,
    order_id            BIGINT,
    customer_id         BIGINT,
    channel             VARCHAR(20)  NOT NULL
        CHECK (channel IN ('SMS','EMAIL','PUSH')),
    recipient           VARCHAR(255) NOT NULL,
    template_code       VARCHAR(50),
    subject             VARCHAR(255),
    body                VARCHAR(4000) NOT NULL,
    status              VARCHAR(20)  NOT NULL
        CHECK (status IN ('PENDING','SENT','FAILED','RETRYING','CANCELLED')),
    attempts            INTEGER      NOT NULL DEFAULT 0,
    last_error          VARCHAR(1000),
    provider_message_id VARCHAR(100),
    created_at          TIMESTAMP    NOT NULL DEFAULT NOW(),
    updated_at          TIMESTAMP    NOT NULL DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS notification_template (
    id               BIGSERIAL PRIMARY KEY,
    template_code    VARCHAR(50)  NOT NULL UNIQUE,
    channel          VARCHAR(20)  NOT NULL
        CHECK (channel IN ('SMS','EMAIL','PUSH')),
    subject_template VARCHAR(255),
    body_template    VARCHAR(4000) NOT NULL,
    active           BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at       TIMESTAMP    NOT NULL DEFAULT NOW(),
    updated_at       TIMESTAMP    NOT NULL DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS notification_attempt (
    id                BIGSERIAL PRIMARY KEY,
    notification_id   BIGINT       NOT NULL REFERENCES notification (id),
    attempt_no        INTEGER      NOT NULL,
    status            VARCHAR(20)  NOT NULL
        CHECK (status IN ('PENDING','SENT','FAILED','RETRYING','CANCELLED')),
    provider_response VARCHAR(1000),
    created_at        TIMESTAMP    NOT NULL DEFAULT NOW()
);
