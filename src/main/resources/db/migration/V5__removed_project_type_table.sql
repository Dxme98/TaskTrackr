ALTER TABLE projects
    ADD COLUMN type VARCHAR(32);

UPDATE projects p
SET type = CASE
               WHEN project_type_id = 1 THEN 'BASIC'
               WHEN project_type_id = 2 THEN 'SCRUM'
    END;

ALTER TABLE projects ALTER COLUMN type SET NOT NULL;

ALTER TABLE projects DROP COLUMN project_type_id;

DROP TABLE IF EXISTS project_type;