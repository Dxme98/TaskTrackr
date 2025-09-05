ALTER TABLE project_roles
    ADD COLUMN role_type VARCHAR(20) NOT NULL DEFAULT 'CUSTOM',
    ADD CONSTRAINT chk_role_type
        CHECK (role_type IN ('OWNER', 'BASE', 'CUSTOM'));