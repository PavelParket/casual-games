--liquibase formatted sql

--changeset Pavel:V0.5.29052026_1400__add_subscription_permissions

INSERT INTO permissions (attribute, operation, created_at)
VALUES ('SUBSCRIPTION', 'READ', CURRENT_TIMESTAMP),
       ('SUBSCRIPTION', 'UPDATE', CURRENT_TIMESTAMP);

INSERT INTO role_permission (role_id, permission_id, for_me, for_all, created_at)
SELECT r.id, p.id, r.for_me, r.for_all, CURRENT_TIMESTAMP
FROM permissions p
    CROSS JOIN (VALUES
                    ((SELECT id FROM roles WHERE name = 'USER'), true, false, CURRENT_TIMESTAMP),
                    ((SELECT id FROM roles WHERE name = 'ADMIN'), true, true, CURRENT_TIMESTAMP)
    ) AS r(id, for_me, for_all, created_at)
WHERE p.attribute = 'SUBSCRIPTION'
  AND p.operation = 'READ';

INSERT INTO role_permission (role_id, permission_id, for_me, for_all, created_at)
SELECT r.id, p.id, r.for_me, r.for_all, CURRENT_TIMESTAMP
FROM permissions p
    CROSS JOIN (VALUES
                    ((SELECT id FROM roles WHERE name = 'USER'), true, false, CURRENT_TIMESTAMP),
                    ((SELECT id FROM roles WHERE name = 'ADMIN'), true, false, CURRENT_TIMESTAMP)
    ) AS r(id, for_me, for_all, created_at)
WHERE p.attribute = 'SUBSCRIPTION'
  AND p.operation = 'UPDATE';