ALTER TABLE comments ADD COLUMN sprint_backlog_item_id BIGINT;

UPDATE comments c
SET sprint_backlog_item_id = sbi.id
FROM sprint_backlog_items sbi
WHERE c.user_story_id = sbi.user_story_id;

ALTER TABLE comments ALTER COLUMN sprint_backlog_item_id SET NOT NULL;
ALTER TABLE comments DROP COLUMN user_story_id;

ALTER TABLE comments
    ADD CONSTRAINT fk_comments_on_sprint_backlog_item
        FOREIGN KEY (sprint_backlog_item_id)
            REFERENCES sprint_backlog_items(id)
            ON DELETE CASCADE;