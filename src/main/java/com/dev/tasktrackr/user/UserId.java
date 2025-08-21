package com.dev.tasktrackr.user;

public record UserId(String value) {
    public UserId {
        if(value == null || value.isEmpty()) {
            throw new IllegalArgumentException("User ID can not be null or empty");
        }
    }
}
