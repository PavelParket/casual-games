--liquibase formatted sql
--changeset Pavel:V0.5.21052026_1000__create_subscription_tables

CREATE TABLE subscription_plans
(
    id     BIGSERIAL PRIMARY KEY,
    status VARCHAR(50)    NOT NULL UNIQUE,
    price  DECIMAL(19, 2) NOT NULL,
    tier   INT            NOT NULL
);

INSERT INTO subscription_plans (status, price, tier)
VALUES ('DEFAULT', 0.00, 0),
       ('PRO', 5000.00, 1),
       ('VIP', 10000.00, 2);

CREATE TABLE user_subscriptions
(
    id               BIGSERIAL PRIMARY KEY,
    user_guid        UUID      NOT NULL UNIQUE,
    started_at       TIMESTAMP NOT NULL,
    expires_at       TIMESTAMP NOT NULL,
    auto_renew       BOOLEAN   NOT NULL DEFAULT TRUE,
    new_status       VARCHAR(50),
    status_change_at TIMESTAMP,
    created_at       TIMESTAMP NOT NULL,
    updated_at       TIMESTAMP NOT NULL,
    version          BIGINT    NOT NULL DEFAULT 0,

    CONSTRAINT fk_user_subscriptions_user_guid
        FOREIGN KEY (user_guid) REFERENCES users (guid) ON DELETE CASCADE
);

CREATE INDEX idx_user_subscriptions_expires_at
    ON user_subscriptions (expires_at);

CREATE INDEX idx_user_subscriptions_status_change_at
    ON user_subscriptions (status_change_at)
    WHERE status_change_at IS NOT NULL;