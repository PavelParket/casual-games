--liquibase formatted sql

--changeset Pavel:V0.1.01012026_1232__temp_init_test_data

INSERT INTO roles (name, created_at) VALUES
    ('USER', CURRENT_TIMESTAMP),
    ('ADMIN', CURRENT_TIMESTAMP)
ON CONFLICT (name) DO NOTHING;

INSERT INTO permissions (attribute, operation, created_at) VALUES
    ('GUID', 'READ', CURRENT_TIMESTAMP),
    ('USERNAME', 'READ', CURRENT_TIMESTAMP),
    ('USERNAME', 'UPDATE', CURRENT_TIMESTAMP),
    ('EMAIL', 'READ', CURRENT_TIMESTAMP),
    ('EMAIL', 'UPDATE', CURRENT_TIMESTAMP),
    ('BALANCE', 'READ', CURRENT_TIMESTAMP),
    ('BALANCE', 'UPDATE', CURRENT_TIMESTAMP),
    ('ROLE', 'READ', CURRENT_TIMESTAMP),
    ('ROLE', 'UPDATE', CURRENT_TIMESTAMP),
    ('STATUS', 'READ', CURRENT_TIMESTAMP),
    ('STATUS', 'UPDATE', CURRENT_TIMESTAMP),

    ('USER', 'CREATE', CURRENT_TIMESTAMP),
    ('USER', 'READ', CURRENT_TIMESTAMP),
    ('USER', 'UPDATE', CURRENT_TIMESTAMP),
    ('USER', 'DELETE', CURRENT_TIMESTAMP)
ON CONFLICT (attribute, operation) DO NOTHING;

INSERT INTO role_permission (role_id, permission_id, for_me, for_all, created_at)
SELECT
    r.id,
    p.id,
    true,
    false,
    CURRENT_TIMESTAMP
FROM roles r
CROSS JOIN permissions p
WHERE r.name = 'USER'
  AND (
      (p.attribute = 'GUID' AND p.operation = 'READ')
      OR (p.attribute = 'USERNAME' AND p.operation = 'READ')
      OR (p.attribute = 'USERNAME' AND p.operation = 'UPDATE')
      OR (p.attribute = 'EMAIL' AND p.operation = 'READ')
      OR (p.attribute = 'EMAIL' AND p.operation = 'UPDATE')
      OR (p.attribute = 'BALANCE' AND p.operation = 'READ')
      OR (p.attribute = 'ROLE' AND p.operation = 'READ')
      OR (p.attribute = 'STATUS' AND p.operation = 'READ')
      OR (p.attribute = 'USER' AND p.operation = 'READ')
      OR (p.attribute = 'USER' AND p.operation = 'UPDATE')
  )
ON CONFLICT (role_id, permission_id) DO NOTHING;

INSERT INTO role_permission (role_id, permission_id, for_me, for_all, created_at)
SELECT
    r.id,
    p.id,
    true,
    true,
    CURRENT_TIMESTAMP
FROM roles r
CROSS JOIN permissions p
WHERE r.name = 'ADMIN'
ON CONFLICT (role_id, permission_id) DO NOTHING;
