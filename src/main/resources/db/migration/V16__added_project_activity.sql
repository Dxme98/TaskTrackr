CREATE TYPE activity_type AS ENUM (
    'CHANGED_PROJECT_NAME',
    'CHANGED_PROJECT_DESCRIPTION',
    'USER_JOINED_PROJECT',
    'USER_REMOVED',
    'USER_LEFT_PROJECT',
    'TASK_CREATED',
    'TASK_DELETED',
    'TASK_COMPLETED',
    'LINK_ADDED',
    'PROJECT_INFO_UPDATED',
    'ROLE_CREATED',
    'ROLE_ASSIGNED',
    'ROLE_DELETED'
    );

CREATE TYPE target_type AS ENUM (
    'PROJECT_MEMBER',
    'TASK',
    'ROLE',
    'PROJECT'
);

CREATE TABLE IF NOT EXISTS project_activity (
     id BIGSERIAL primary key,
     project_id BIGINT NOT NULL,
     activity_Type activity_type NOT NULL,
     actor_id BIGINT,
     actor_name varchar(50) NOT NULL,
     target_id BIGINT,
     target_name varchar(50),
     target_type target_type,
     context TEXT,
     created_at TIMESTAMP,

    FOREIGN KEY (project_id) REFERENCES projects(id) ON DELETE CASCADE,
    FOREIGN KEY (actor_id) REFERENCES project_member(id) ON DELETE SET NULL
);