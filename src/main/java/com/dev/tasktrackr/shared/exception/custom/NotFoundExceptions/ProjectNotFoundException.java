package com.dev.tasktrackr.shared.exception.custom.NotFoundExceptions;

import com.dev.tasktrackr.shared.exception.ErrorCode;
import com.dev.tasktrackr.shared.exception.custom.AppException;
import org.springframework.http.HttpStatus;

public class ProjectNotFoundException extends AppException {

    public ProjectNotFoundException(Long projectId) {
        super("Project with id " + projectId + " not found", ErrorCode.PROJECT_NOT_FOUND, HttpStatus.NOT_FOUND);
    }
}
