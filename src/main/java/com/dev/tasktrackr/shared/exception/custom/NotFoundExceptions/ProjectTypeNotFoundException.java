package com.dev.tasktrackr.shared.exception.custom.NotFoundExceptions;

import com.dev.tasktrackr.shared.exception.ErrorCode;
import com.dev.tasktrackr.shared.exception.custom.AppException;
import org.springframework.http.HttpStatus;

public class ProjectTypeNotFoundException extends AppException {
    public ProjectTypeNotFoundException(int projectTypeId) {
        super("ProjectType with ID: " + projectTypeId + " not found", ErrorCode.PROJECT_TYPE_NOT_FOUND, HttpStatus.NOT_FOUND);
    }
}
