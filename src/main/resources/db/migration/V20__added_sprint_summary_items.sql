CREATE TABLE IF NOT EXISTS sprint_summary_items (
       id BIGSERIAL PRIMARY KEY,
       project_id BIGINT NOT NULL,
       sprint_id BIGINT NOT NULL,
       user_story_id BIGINT NOT NULL,

       title varchar(255) NOT NULL,
       story_points INT CHECK (story_points IN (1, 2, 3, 5, 8, 13, 20, 40, 100)),
       is_completed BOOLEAN NOT NULL DEFAULT false,

       UNIQUE(sprint_id, user_story_id),

       FOREIGN KEY (project_id) REFERENCES projects(id) ON DELETE CASCADE,
       FOREIGN KEY (sprint_id) REFERENCES sprints(id) ON DELETE CASCADE,
       FOREIGN KEY (user_story_id) REFERENCES user_stories(id) ON DELETE CASCADE
)