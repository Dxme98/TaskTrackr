ALTER TABLE project_member
    ADD COLUMN role_id INT,
    ADD CONSTRAINT fk_project_member_role
    FOREIGN KEY (role_id) REFERENCES project_roles(id) ON DELETE SET NULL;
