-- 1. Spalten auf VARCHAR umstellen und gültige Werte einschränken
ALTER TABLE tasks
    ALTER COLUMN priority DROP DEFAULT,
    ALTER COLUMN priority TYPE VARCHAR(20) USING priority::text,
    ADD CONSTRAINT chk_tasks_priority CHECK (priority IN ('LOW', 'MEDIUM', 'HIGH'));

ALTER TABLE tasks
    ALTER COLUMN status DROP DEFAULT,
    ALTER COLUMN status TYPE VARCHAR(20) USING status::text,
    ADD CONSTRAINT chk_tasks_status CHECK (status IN ('IN_PROGRESS', 'EXPIRED', 'COMPLETED'));

-- Default nach Typänderung wieder setzen (als String)
ALTER TABLE tasks
    ALTER COLUMN status SET DEFAULT 'IN_PROGRESS';

ALTER TABLE links
    ALTER COLUMN type TYPE VARCHAR(20) USING type::text,
    ADD CONSTRAINT chk_links_type CHECK (type IN ('GITHUB', 'DOCS', 'WEB'));

-- 2. Alte Enum-Typen löschen (nur wenn keine andere Tabelle sie nutzt!)
DROP TYPE IF EXISTS task_priority;
DROP TYPE IF EXISTS task_status;
DROP TYPE IF EXISTS link_type;