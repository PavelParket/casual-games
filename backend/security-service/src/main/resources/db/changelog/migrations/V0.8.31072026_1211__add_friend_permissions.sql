--liquibase formatted sql

--changeset Pavel:V0.8.31072026_1211__add_friend_permissions.sql

INSERT INTO permissions (attribute, operation, created_at)
VALUES ('FRIEND', 'READ', CURRENT_TIMESTAMP),
       ('FRIEND', 'DELETE', CURRENT_TIMESTAMP),
       ('FRIEND_REQUEST', 'READ', CURRENT_TIMESTAMP),
       ('FRIEND_REQUEST', 'UPDATE', CURRENT_TIMESTAMP);

INSERT INTO role_permission (role_id, permission_id, for_me, for_all, created_at)
SELECT r.id, p.id, true, false, CURRENT_TIMESTAMP
FROM roles r
CROSS JOIN permissions p
WHERE r.name = 'USER'
  AND p.attribute IN ('FRIEND', 'FRIEND_REQUEST')
  AND p.operation IN ('READ', 'DELETE', 'UPDATE');

INSERT INTO role_permission (role_id, permission_id, for_me, for_all, created_at)
SELECT r.id, p.id, true, true, CURRENT_TIMESTAMP
FROM roles r
CROSS JOIN permissions p
WHERE r.name = 'ADMIN'
  AND p.attribute IN ('FRIEND', 'FRIEND_REQUEST')
  AND p.operation IN ('READ', 'DELETE', 'UPDATE');
