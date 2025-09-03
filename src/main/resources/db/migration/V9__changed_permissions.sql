DROP TABLE IF EXISTS role_permissions;
DROP TABLE IF EXISTS permissions;

CREATE TABLE IF NOT EXISTS role_permissions (
      role_id INT NOT NULL,
      permission_name VARCHAR(64) NOT NULL CHECK (permission_name IN (
                 'BASIC_CREATE_TASK',
                 'BASIC_DELETE_TASK',
                 'BASIC_EDIT_INFORMATION',
                 'COMMON_INVITE_USER',
                 'COMMON_REMOVE_USER',
                 'COMMON_MANAGE_ROLES'
                 )),
      PRIMARY KEY (role_id, permission_name),
      FOREIGN KEY (role_id) REFERENCES project_roles(id) ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS idx_role_permissions_permission_name ON role_permissions(permission_name);