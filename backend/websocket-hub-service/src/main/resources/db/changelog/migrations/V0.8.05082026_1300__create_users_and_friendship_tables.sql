--liquibase formatted sql

--changeset Pavel:V0.8.05082026_1300__create_users_and_friendship_tables

CREATE TABLE users
(
    id                        BIGSERIAL PRIMARY KEY,
    guid                      UUID UNIQUE NOT NULL,
    username                  VARCHAR(50) NOT NULL,
    status                    VARCHAR(50) NOT NULL DEFAULT 'DEFAULT',
    link_profile_picture      VARCHAR(512),
    link_profile_picture_mini VARCHAR(512),
    created_at                TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE friendship
(
    id          BIGSERIAL PRIMARY KEY,
    user_guid   UUID      NOT NULL,
    friend_guid UUID      NOT NULL,
    created_at  TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE UNIQUE INDEX uq_friendship ON friendship (LEAST(user_guid, friend_guid), GREATEST(user_guid, friend_guid));
