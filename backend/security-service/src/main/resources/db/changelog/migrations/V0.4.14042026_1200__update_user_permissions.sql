--liquibase formatted sql

--changeset Pavel:V0.4.14042026_1200__update_user_permissions

DELETE FROM role_permission
WHERE permission_id IN (SELECT id FROM permissions
                        WHERE attribute = 'USER'
                          AND operation IN ('READ', 'CREATE'));

DELETE FROM permissions
WHERE attribute = 'USER'
  AND operation IN ('READ', 'CREATE');

UPDATE role_permission
SET for_all = true
WHERE role_id = (SELECT id FROM roles WHERE name = 'USER')
  AND permission_id IN (SELECT id FROM permissions
                        WHERE attribute IN ('GUID', 'USERNAME')
                          AND operation = 'READ');