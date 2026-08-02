--liquibase formatted sql

--changeset Pavel:V0.8.31072026_1143__create_friends_tables.sql

CREATE EXTENSION IF NOT EXISTS pg_trgm;

CREATE TABLE friend_request
(
    id             BIGSERIAL PRIMARY KEY,
    requester_guid UUID        NOT NULL,
    recipient_guid UUID        NOT NULL,
    status         VARCHAR(64) NOT NULL DEFAULT 'PENDING',
    created_at     TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP,
    resolved_at    TIMESTAMP
);

CREATE INDEX uq_friend_request_pending ON friend_request (requester_guid, recipient_guid)
    WHERE status = 'PENDING';

CREATE INDEX idx_friend_request_incoming ON friend_request (recipient_guid, status, created_at);
CREATE INDEX idx_friend_request_outgoing ON friend_request (requester_guid, status, created_at);

CREATE TABLE friendship
(
    id          BIGSERIAL PRIMARY KEY,
    user_guid   UUID      NOT NULL,
    friend_guid UUID      NOT NULL,
    created_at  TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_users_username_trgm ON users USING GIN (lower(username) gin_trgm_ops);
CREATE UNIQUE INDEX uq_friendship ON friendship (LEAST(user_guid, friend_guid), GREATEST(user_guid, friend_guid));
