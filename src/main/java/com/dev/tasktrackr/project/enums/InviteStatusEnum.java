package com.dev.tasktrackr.project.enums;

import lombok.Getter;

@Getter
public enum InviteStatusEnum {
    PENDING(1, "PENDING"),
    ACCEPTED(2, "ACCEPTED"),
    DECLINED(3, "DECLINED");

    private final int id;
    private final String name;

    InviteStatusEnum(int id, String name) {
        this.id = id;
        this.name = name;
    }
}
