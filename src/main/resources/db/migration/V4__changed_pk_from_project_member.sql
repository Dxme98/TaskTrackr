ALTER TABLE project_member
    ADD COLUMN id BIGSERIAL;

ALTER TABLE project_member
    DROP CONSTRAINT project_member_pkey;

ALTER TABLE project_member
    ADD CONSTRAINT project_member_pkey PRIMARY KEY (id);

ALTER TABLE project_member
    ADD CONSTRAINT uk_project_member_user_project UNIQUE (user_id, project_id);