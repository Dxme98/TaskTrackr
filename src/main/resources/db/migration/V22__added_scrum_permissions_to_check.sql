ALTER TABLE role_permissions
    DROP CONSTRAINT "role_permissions_permission_name_check";

ALTER TABLE role_permissions
    ADD CONSTRAINT chk_permission_name CHECK (permission_name IN (
        -- Alte Permissions
       'BASIC_CREATE_TASK',
       'BASIC_DELETE_TASK',
       'BASIC_EDIT_INFORMATION',
       'COMMON_INVITE_USER',
       'COMMON_REMOVE_USER',
       'COMMON_MANAGE_ROLES',

        -- Neue Scrum Permissions
       'SCRUM_CREATE_USER_STORY',
       'SCRUM_DELETE_USER_STORY',
       'SCRUM_PLAN_SPRINT',
       'SCRUM_START_SPRINT',
       'SCRUM_END_SPRINT',
       'SCRUM_ASSIGN_USER_TO_STORY',
       'SCRUM_UPDATE_STORY_STATUS',
       'SCRUM_CAN_DELETE_COMMENTS_AND_BLOCKER'
    ));