--liquibase formatted sql

--changeset Pavel:V0.6.04072026_0137__add_notification_permissions

INSERT INTO permissions (attribute, operation, created_at)
VALUES ('NOTIFICATION', 'READ', CURRENT_TIMESTAMP),
       ('NOTIFICATION', 'UPDATE', CURRENT_TIMESTAMP);

INSERT INTO role_permission (role_id, permission_id, for_me, for_all, created_at)
SELECT r.id, p.id, true, false, CURRENT_TIMESTAMP
FROM roles r
CROSS JOIN permissions p
WHERE r.name = 'USER'
  AND p.attribute = 'NOTIFICATION'
  AND p.operation IN ('READ', 'UPDATE');

INSERT INTO role_permission (role_id, permission_id, for_me, for_all, created_at)
SELECT r.id, p.id, true, true, CURRENT_TIMESTAMP
FROM roles r
CROSS JOIN permissions p
WHERE r.name = 'ADMIN'
  AND p.attribute = 'NOTIFICATION'
  AND p.operation IN ('READ', 'UPDATE');