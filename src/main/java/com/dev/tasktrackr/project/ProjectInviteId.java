package com.dev.tasktrackr.project;

public record ProjectInviteId(Long value) {
    public ProjectInviteId {
        if(value == null || value <= 0) {
            throw new IllegalArgumentException("Invite ID must be positive");
        }
    }
}
