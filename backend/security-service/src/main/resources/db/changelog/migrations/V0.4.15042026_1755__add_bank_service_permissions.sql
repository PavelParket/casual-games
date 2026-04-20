--liquibase formatted sql

--changeset Pavel:V0.4.15042026_1755__add_bank_service_permissions

INSERT INTO permissions (attribute, operation, created_at)
VALUES ('TRANSACTION', 'READ', CURRENT_TIMESTAMP),
       ('TRANSACTION_SUMMARY', 'READ', CURRENT_TIMESTAMP),
       ('TRANSACTION_SUMMARY', 'UPDATE', CURRENT_TIMESTAMP);

INSERT INTO role_permission (role_id, permission_id, for_me, for_all, created_at)
SELECT r.id, p.id, true, false, CURRENT_TIMESTAMP
FROM roles r CROSS JOIN permissions p
WHERE r.name = 'USER'
  AND p.attribute = 'TRANSACTION'
  AND p.operation = 'READ';

INSERT INTO role_permission (role_id, permission_id, for_me, for_all, created_at)
SELECT r.id, p.id, true, true, CURRENT_TIMESTAMP
FROM roles r CROSS JOIN permissions p
WHERE r.name = 'ADMIN'
  AND p.attribute = 'TRANSACTION'
  AND p.operation = 'READ';

INSERT INTO role_permission (role_id, permission_id, for_me, for_all, created_at)
SELECT r.id, p.id, true, false, CURRENT_TIMESTAMP
FROM roles r CROSS JOIN permissions p
WHERE r.name = 'USER'
  AND p.attribute = 'TRANSACTION_SUMMARY'
  AND p.operation = 'READ';

INSERT INTO role_permission (role_id, permission_id, for_me, for_all, created_at)
SELECT r.id, p.id, true, true, CURRENT_TIMESTAMP
FROM roles r CROSS JOIN permissions p
WHERE r.name = 'ADMIN'
  AND p.attribute = 'TRANSACTION_SUMMARY'
  AND p.operation = 'READ';

INSERT INTO role_permission (role_id, permission_id, for_me, for_all, created_at)
SELECT r.id, p.id, true, true, CURRENT_TIMESTAMP
FROM roles r CROSS JOIN permissions p
WHERE r.name = 'ADMIN'
  AND p.attribute = 'TRANSACTION_SUMMARY'
  AND p.operation = 'UPDATE';

INSERT INTO role_permission (role_id, permission_id, for_me, for_all, created_at)
SELECT r.id, p.id, true, false, CURRENT_TIMESTAMP
FROM roles r CROSS JOIN permissions p
WHERE r.name = 'USER'
  AND p.attribute = 'BALANCE'
  AND p.operation = 'UPDATE';
