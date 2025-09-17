CREATE TYPE task_priority AS ENUM ('LOW', 'MEDIUM', 'HIGH');
ALTER TABLE tasks DROP CONSTRAINT chk_tasks_priority;

ALTER TABLE tasks
    ALTER COLUMN priority TYPE task_priority
        USING priority::task_priority;