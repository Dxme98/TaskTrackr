-- Umstieg auf Spring Data Auditing (App Seite)

-- Users: created_at von DB-Default befreien
ALTER TABLE users
    ALTER COLUMN created_at DROP DEFAULT;

-- Projects: created_at von DB-Default befreien, updated_at hinzufügen
ALTER TABLE projects
    ADD COLUMN updated_at TIMESTAMP WITH TIME ZONE;

ALTER TABLE projects
    ALTER COLUMN created_at DROP DEFAULT;

-- ProjectMember: created_at hinzufügen
ALTER TABLE public.project_member
    ADD COLUMN created_at TIMESTAMP WITH TIME ZONE;

-- Project_Invite: beide Spalten von Default befreien
ALTER TABLE project_invite
    ALTER COLUMN created_at DROP DEFAULT,
    ALTER COLUMN updated_at DROP DEFAULT;