package com.dev.tasktrackr.project.domain.enums;

import lombok.Getter;

@Getter
public enum PermissionName {
    BASIC_CREATE_TASK(1, "BASIC_CREATE_TASK"),
    BASIC_DELETE_TASK(2, "BASIC_DELETE_TASK"),
    BASIC_EDIT_INFORMATION(3, "BASIC_EDIT_INFORMATION"),
    COMMON_INVITE_USER(4, "COMMON_INVITE_USER"),
    COMMON_REMOVE_USER(5, "COMMON_REMOVE_USER"),
    COMMON_CREATE_ROLE(6, "COMMON_CREATE_ROLE"),
    COMMON_ASSIGN_ROLE(7, "COMMON_ASSIGN_ROLE");

    private final int id;
    private final String name;

    PermissionName(int id, String name) {
        this.id = id;
        this.name = name;
    }
}
