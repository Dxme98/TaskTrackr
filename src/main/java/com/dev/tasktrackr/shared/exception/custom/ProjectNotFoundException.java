package com.dev.tasktrackr.shared.exception.custom;

import com.dev.tasktrackr.shared.exception.ErrorCode;

public class ProjectNotFoundException extends ResourceNotFoundException{

    public ProjectNotFoundException(Long projectId) {
        super("Project with id " + projectId + " not found", ErrorCode.PROJECT_NOT_FOUND);
    }
}
