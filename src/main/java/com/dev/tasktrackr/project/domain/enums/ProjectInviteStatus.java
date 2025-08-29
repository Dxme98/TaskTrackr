package com.dev.tasktrackr.project.domain.enums;

import lombok.Getter;

@Getter
public enum ProjectInviteStatus {
    PENDING(1, "PENDING"),
    ACCEPTED(2, "ACCEPTED"),
    DECLINED(3, "DECLINED");

    private final int id;
    private final String name;

    ProjectInviteStatus(int id, String name) {
        this.id = id;
        this.name = name;
    }
}
