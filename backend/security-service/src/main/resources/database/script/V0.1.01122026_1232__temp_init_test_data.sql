-- Insert default roles
INSERT INTO roles (name, created_at) VALUES
    ('USER', CURRENT_TIMESTAMP),
    ('ADMIN', CURRENT_TIMESTAMP)
ON CONFLICT (name) DO NOTHING;

-- Insert default permissions
INSERT INTO permissions (attribute, operation, created_at) VALUES
    -- User attributes
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

    -- User management
    ('USER', 'CREATE', CURRENT_TIMESTAMP),
    ('USER', 'READ', CURRENT_TIMESTAMP),
    ('USER', 'UPDATE', CURRENT_TIMESTAMP),
    ('USER', 'DELETE', CURRENT_TIMESTAMP)
ON CONFLICT (attribute, operation) DO NOTHING;

-- Assign permissions to USER role (basic access)
INSERT INTO role_permission (role_id, permission_id, for_me, for_all, created_at)
SELECT
    r.id,
    p.id,
    true,  -- for_me: can access own data
    false, -- for_all: cannot access others' data
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
      OR (p.attribute = 'USER' AND p.operation = 'READ') -- can read own profile
      OR (p.attribute = 'USER' AND p.operation = 'UPDATE') -- can update own profile
  )
ON CONFLICT (role_id, permission_id) DO NOTHING;

-- Assign permissions to ADMIN role (full access)
INSERT INTO role_permission (role_id, permission_id, for_me, for_all, created_at)
SELECT
    r.id,
    p.id,
    true,  -- for_me
    true,  -- for_all: can access everything
    CURRENT_TIMESTAMP
FROM roles r
CROSS JOIN permissions p
WHERE r.name = 'ADMIN'
ON CONFLICT (role_id, permission_id) DO NOTHING;

-- Verify data
SELECT
    r.name as role,
    COUNT(*) as permission_count
FROM role_permission rp
JOIN roles r ON rp.role_id = r.id
GROUP BY r.name
ORDER BY r.name;