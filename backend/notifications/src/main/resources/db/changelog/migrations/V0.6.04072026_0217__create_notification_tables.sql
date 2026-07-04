--liquibase formatted sql

--changeset Pavel:V0.6.04072026_0217__create_notification_tables

CREATE TABLE notifications
(
    id             BIGSERIAL PRIMARY KEY,
    recipient_guid UUID         NOT NULL,
    type           VARCHAR(64)  NOT NULL,
    title          VARCHAR(255) NOT NULL,
    body           TEXT         NOT NULL,
    link           VARCHAR(512),
    expires_at     TIMESTAMP,
    event_id       UUID         NOT NULL,
    read_at        TIMESTAMP,
    created_at     TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT uq_notifications_event_id UNIQUE (event_id)
);

CREATE INDEX idx_notifications_recipient_created ON notifications (recipient_guid, created_at DESC);

CREATE INDEX idx_notifications_unread ON notifications (recipient_guid)
    WHERE read_at IS NULL;

CREATE TABLE notification_templates
(
    id               BIGSERIAL PRIMARY KEY,
    type             VARCHAR(64) UNIQUE NOT NULL,
    title_template   VARCHAR(255)       NOT NULL,
    body_template    TEXT               NOT NULL,
    link_template    VARCHAR(512),
    enabled          BOOLEAN            NOT NULL DEFAULT TRUE,
    created_at       TIMESTAMP          NOT NULL DEFAULT CURRENT_TIMESTAMP,
    last_modified_at TIMESTAMP          NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE announcements
(
    id         BIGSERIAL PRIMARY KEY,
    title      VARCHAR(255) NOT NULL,
    body       TEXT         NOT NULL,
    created_by UUID         NOT NULL,
    created_at TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_announcements_created ON announcements (created_at DESC);

CREATE TABLE user_announcement_state
(
    user_guid                 UUID PRIMARY KEY,
    last_seen_announcement_id BIGINT NOT NULL DEFAULT 0
);

INSERT INTO notification_templates (type, title_template, body_template, link_template, enabled)
VALUES ('SUBSCRIPTION_EXPIRING_SOON', 'Subscription expiring soon', 'Hi, {username}! Your {tier} subscription expires in {daysLeft} day(s).', NULL, TRUE),
       ('FRIEND_REQUEST_RECEIVED', 'New friend request', '{username} wants to add you as a friend.', NULL, TRUE),
       ('FRIEND_REQUEST_ACCEPTED', 'Friend request accepted', '{username} accepted your friend request.', NULL, TRUE),
       ('ROOM_INVITE', 'Game invite', '{username} invites you to a room.', NULL, TRUE);
