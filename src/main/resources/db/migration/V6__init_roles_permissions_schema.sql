CREATE TABLE IF NOT EXISTS project_roles (
     id SERIAL PRIMARY KEY,
     name VARCHAR(36) NOT NULL,
     project_id BIGINT NOT NULL,
     FOREIGN KEY (project_id) REFERENCES projects(id) ON DELETE CASCADE,
     UNIQUE(project_id, name)
);

CREATE TABLE IF NOT EXISTS permissions (
     id SERIAL PRIMARY KEY,
     name VARCHAR(64) UNIQUE NOT NULL,
     type VARCHAR(32) NOT NULL CHECK (type IN ('BASIC', 'SCRUM', 'COMMON'))
);

CREATE TABLE IF NOT EXISTS role_permissions (
     role_id INT NOT NULL,
     permission_id INT NOT NULL,
     PRIMARY KEY (role_id, permission_id),
     FOREIGN KEY (role_id) REFERENCES project_roles(id) ON DELETE CASCADE,
     FOREIGN KEY (permission_id) REFERENCES permissions(id) ON DELETE CASCADE
);

-- Seed Permissions
INSERT INTO permissions (name, type) VALUES
     ('BASIC_CREATE_TASK', 'BASIC'),
     ('BASIC_DELETE_TASK', 'BASIC'),
     ('BASIC_EDIT_INFORMATION', 'BASIC'),
     ('COMMON_INVITE_USER', 'COMMON'),
     ('COMMON_REMOVE_USER', 'COMMON'),
     ('COMMON_CREATE_ROLE', 'COMMON'),
     ('COMMON_ASSIGN_ROLE', 'COMMON')
ON CONFLICT DO NOTHING;
