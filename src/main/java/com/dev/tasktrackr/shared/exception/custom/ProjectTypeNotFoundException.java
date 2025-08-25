package com.dev.tasktrackr.shared.exception.custom;

import com.dev.tasktrackr.shared.exception.ErrorCode;

public class ProjectTypeNotFoundException extends ResourceNotFoundException {
    public ProjectTypeNotFoundException(int projectTypeId) {
        super("ProjectType with ID: " + projectTypeId + " not found", ErrorCode.PROJECT_TYPE_NOT_FOUND);
    }
}
