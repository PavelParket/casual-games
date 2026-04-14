--liquibase formatted sql

--changeset Pavel:V0.4.04042026_1200__create_user_permission_table

ALTER TABLE auths
    ALTER COLUMN created_at SET DEFAULT CURRENT_TIMESTAMP;

ALTER TABLE roles
    ALTER COLUMN created_at SET DEFAULT CURRENT_TIMESTAMP;

ALTER TABLE permissions
    ALTER COLUMN created_at SET DEFAULT CURRENT_TIMESTAMP;

ALTER TABLE role_permission
    ALTER COLUMN created_at SET DEFAULT CURRENT_TIMESTAMP;

CREATE TABLE user_permission
(
    id            BIGSERIAL PRIMARY KEY,
    user_guid     uuid      NOT NULL REFERENCES auths (guid) ON DELETE CASCADE,
    permission_id BIGINT    NOT NULL REFERENCES permissions (id) ON DELETE CASCADE,
    for_me        BOOLEAN   NOT NULL,
    for_all       BOOLEAN   NOT NULL,
    allowed       BOOLEAN   NOT NULL,
    created_at    TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE (user_guid, permission_id)
);

CREATE INDEX IF NOT EXISTS idx_user_permission_user_id ON user_permission (user_guid);
CREATE INDEX IF NOT EXISTS idx_user_permission_permission_id ON user_permission (permission_id);
