package com.dev.tasktrackr.project.enums;

import lombok.Getter;

@Getter
public enum InviteStatus {
    PENDING(1, "PENDING"),
    ACCEPTED(2, "ACCEPTED"),
    DECLINED(3, "DECLINED");

    private final int id;
    private final String name;

    InviteStatus(int id, String name) {
        this.id = id;
        this.name = name;
    }
}
