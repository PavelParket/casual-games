--liquibase formatted sql

--changeset Timofei:V0.5.08052026_1437__update_read_status_permission

UPDATE role_permission
SET for_all = true
WHERE permission_id IN (
    SELECT id FROM permissions
    WHERE attribute = 'STATUS'
      AND operation = 'READ'
)
AND role_id = (SELECT id FROM roles WHERE name = 'USER');