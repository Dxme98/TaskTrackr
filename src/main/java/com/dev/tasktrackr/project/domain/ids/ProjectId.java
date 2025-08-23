package com.dev.tasktrackr.project.domain.ids;

public record ProjectId(Long value) {
    public ProjectId {
        if(value == null || value <= 0) {
            throw new IllegalArgumentException("Project ID must be positive");
        }
    }
}
