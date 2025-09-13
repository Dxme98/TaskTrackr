-- BASIC_DETAILS anpassen
ALTER TABLE basic_details DROP CONSTRAINT IF EXISTS basic_details_pkey;
ALTER TABLE basic_details DROP CONSTRAINT IF EXISTS basic_details_id_fkey;

ALTER TABLE basic_details RENAME COLUMN id TO project_id;

ALTER TABLE basic_details
    ALTER COLUMN project_id DROP DEFAULT,
    ALTER COLUMN project_id TYPE BIGINT;

ALTER TABLE basic_details
    ADD CONSTRAINT basic_details_pkey PRIMARY KEY (project_id),
    ADD CONSTRAINT fk_basic_project FOREIGN KEY (project_id) REFERENCES projects(id) ON DELETE CASCADE;

-- SCRUM_DETAILS anpassen
ALTER TABLE scrum_details DROP CONSTRAINT IF EXISTS scrum_details_pkey;
ALTER TABLE scrum_details DROP CONSTRAINT IF EXISTS scrum_details_id_fkey;

ALTER TABLE scrum_details RENAME COLUMN id TO project_id;

ALTER TABLE scrum_details
    ALTER COLUMN project_id DROP DEFAULT,
    ALTER COLUMN project_id TYPE BIGINT;

ALTER TABLE scrum_details
    ADD CONSTRAINT scrum_details_pkey PRIMARY KEY (project_id),
    ADD CONSTRAINT fk_scrum_project FOREIGN KEY (project_id) REFERENCES projects(id) ON DELETE CASCADE;