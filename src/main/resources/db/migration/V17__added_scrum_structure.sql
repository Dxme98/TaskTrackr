CREATE TYPE story_status AS ENUM ('NOT_ASSIGNED_TO_SPRINT', 'SPRINT_BACKLOG', 'IN_PROGRESS', 'REVIEW', 'DONE');
CREATE TYPE sprint_status AS ENUM ('PLANNED', 'ACTIVE', 'DONE');
CREATE TYPE comment_type AS ENUM ('COMMENT', 'BLOCKER');

CREATE TABLE IF NOT EXISTS user_stories(
        id BIGSERIAL PRIMARY KEY,
        project_id BIGINT NOT NULL,

        title varchar(255) NOT NULL,
        description TEXT,
        priority task_priority NOT NULL,
        story_points INT CHECK (story_points IN (1, 2, 3, 5, 8, 13, 20, 40, 100)),
        createdAt TIMESTAMP NOT NULL,
        status story_status NOT NULL,

        UNIQUE(project_id, title),
        FOREIGN KEY (project_id) REFERENCES projects(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS sprints (
        id BIGSERIAL PRIMARY KEY,
        project_id BIGINT NOT NULL,

        name VARCHAR(255) NOT NULL,
        description TEXT,
        status sprint_status NOT NULL DEFAULT 'PLANNED',

        start_date DATE NOT NULL,
        end_date DATE NOT NULL,

        FOREIGN KEY (project_id) REFERENCES projects(id) ON DELETE CASCADE,
        CONSTRAINT chk_end_date_after_start_date CHECK (end_date > start_date)
);

CREATE TABLE IF NOT EXISTS sprint_backlog_items (
        id BIGSERIAL PRIMARY KEY,
        sprint_id BIGINT NOT NULL,
        user_story_id BIGINT NOT NULL UNIQUE,

        FOREIGN KEY (sprint_id) REFERENCES sprints(id) ON DELETE CASCADE,
        FOREIGN KEY (user_story_id) REFERENCES user_stories(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS sprint_backlog_item_assignee (
        id BIGSERIAL PRIMARY KEY,
        sprint_backlog_item_id BIGINT NOT NULL,
        assigned_member_id BIGINT NOT NULL,

        UNIQUE(sprint_backlog_item_id, assigned_member_id),
        FOREIGN KEY (sprint_backlog_item_id) REFERENCES sprint_backlog_items(id) ON DELETE CASCADE,
        FOREIGN KEY (assigned_member_id) REFERENCES project_member(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS comments (
       id BIGSERIAL PRIMARY KEY,
       user_story_id BIGINT NOT NULL REFERENCES user_stories(id) ON DELETE CASCADE,
       created_by BIGINT REFERENCES project_member(id) ON DELETE SET NULL,

       message varchar(500) NOT NULL,
       type comment_type NOT NULL,
       created_at TIMESTAMP
);