ALTER TABLE project_invite
    ADD COLUMN status VARCHAR(20) NOT NULL DEFAULT 'PENDING';

ALTER TABLE project_invite
    DROP CONSTRAINT IF EXISTS project_invite_invite_status_id_fkey;

ALTER TABLE project_invite
    DROP COLUMN IF EXISTS invite_status_id;

DROP TABLE IF EXISTS invite_status CASCADE;

ALTER TABLE project_invite
    ADD CONSTRAINT chk_project_invite_status
        CHECK (status IN ('PENDING', 'ACCEPTED', 'DECLINED'));