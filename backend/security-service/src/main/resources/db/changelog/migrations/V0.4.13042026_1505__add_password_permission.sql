--liquibase formatted sql

--changeset Pavel:V0.4.13042026_1505__add_password_permission

INSERT INTO permissions (attribute, operation, created_at) VALUES
    ('PASSWORD', 'UPDATE', CURRENT_TIMESTAMP);

INSERT INTO role_permission (role_id, permission_id, for_me, for_all, created_at)
SELECT r.id, p.id, true, false, CURRENT_TIMESTAMP
FROM roles r
         CROSS JOIN permissions p
WHERE r.name = 'USER'
  AND p.attribute = 'PASSWORD'
  AND p.operation = 'UPDATE';

INSERT INTO role_permission (role_id, permission_id, for_me, for_all, created_at)
SELECT r.id, p.id, true, true, CURRENT_TIMESTAMP
FROM roles r
         CROSS JOIN permissions p
WHERE r.name = 'ADMIN'
  AND p.attribute = 'PASSWORD'
  AND p.operation = 'UPDATE';