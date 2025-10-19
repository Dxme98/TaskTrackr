package com.dev.tasktrackr.project.domain.enums;

import lombok.Getter;

@Getter
public enum PermissionName {
    // Basic Project Permissions
    BASIC_CREATE_TASK(1, "BASIC_CREATE_TASK"),
    BASIC_DELETE_TASK(2, "BASIC_DELETE_TASK"),
    BASIC_EDIT_INFORMATION(3, "BASIC_EDIT_INFORMATION"),

    // Common Project Permissions
    COMMON_INVITE_USER(4, "COMMON_INVITE_USER"),
    COMMON_REMOVE_USER(5, "COMMON_REMOVE_USER"),
    COMMON_MANAGE_ROLES(6, "COMMON_MANAGE_ROLES"),

    // Scrum Project Permissions
    SCRUM_CREATE_USER_STORY(7, "SCRUM_CREATE_USER_STORY"),
    SCRUM_DELETE_USER_STORY(8, "SCRUM_DELETE_USER_STORY"),
    SCRUM_PLAN_SPRINT(9, "SCRUM_PLAN_SPRINT"),
    SCRUM_START_SPRINT(10, "SCRUM_START_SPRINT"),
    SCRUM_END_SPRINT(11, "SCRUM_END_SPRINT"),
    SCRUM_ASSIGN_USER_TO_STORY(12, "SCRUM_ASSIGN_USER_TO_STORY"),
    SCRUM_UPDATE_STORY_STATUS(13, "SCRUM_UPDATE_STORY_STATUS"),
    SCRUM_CAN_DELETE_COMMENTS_AND_BLOCKER(14, "SCRUM_CAN_DELETE_COMMENTS_AND_BLOCKER");




    private final int id;
    private final String name;

    PermissionName(int id, String name) {
        this.id = id;
        this.name = name;
    }
}
