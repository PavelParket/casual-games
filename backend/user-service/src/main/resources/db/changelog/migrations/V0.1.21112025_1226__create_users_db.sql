--liquibase formatted sql

--changeset Pavel:V0.1.21112025_1226__create_users_db

CREATE TABLE users
(
    id         BIGSERIAL PRIMARY KEY,
    guid       UUID UNIQUE         NOT NULL,
    username   VARCHAR(50)         NOT NULL,
    email      VARCHAR(200) UNIQUE NOT NULL,
    balance    DECIMAL(19, 2)      NOT NULL DEFAULT 0.00,
    role       VARCHAR(50)         NOT NULL DEFAULT 'USER',
    status     VARCHAR(50)         NOT NULL DEFAULT 'DEFAULT',
    created_at TIMESTAMP           NOT NULL
);

CREATE UNIQUE INDEX ux_users_guid ON users (guid);
CREATE UNIQUE INDEX ux_users_email ON users (email);