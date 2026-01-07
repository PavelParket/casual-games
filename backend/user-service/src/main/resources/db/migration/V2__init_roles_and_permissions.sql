CREATE TABLE roles (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(50) NOT NULL UNIQUE,
    created_at TIMESTAMP NOT NULL
);

CREATE TABLE permissions (
    id BIGSERIAL PRIMARY KEY,
    attribute VARCHAR(100) NOT NULL,
    operation VARCHAR(50) NOT NULL,
    created_at TIMESTAMP NOT NULL
);

CREATE TABLE role_permission (
    id BIGSERIAL PRIMARY KEY,
    role_id BIGINT NOT NULL REFERENCES roles(id) ON DELETE CASCADE,
    permission_id BIGINT NOT NULL REFERENCES permissions(id) ON DELETE CASCADE,
    for_me BOOLEAN NOT NULL,
    for_all BOOLEAN NOT NULL,
    created_at TIMESTAMP NOT NULL
);