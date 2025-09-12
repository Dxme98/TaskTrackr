package com.dev.tasktrackr.shared.exception.custom.BadRequestExceptions;

import com.dev.tasktrackr.project.domain.enums.PermissionName;
import com.dev.tasktrackr.project.domain.enums.ProjectType;
import com.dev.tasktrackr.shared.exception.ErrorCode;
import com.dev.tasktrackr.shared.exception.custom.AppException;
import org.springframework.http.HttpStatus;

public class InvalidRoleAssignmentException extends AppException {

    public InvalidRoleAssignmentException(PermissionName permission, ProjectType projectType) {
        super("Permission: " + permission + " does not match Projecttype: " + projectType,
                ErrorCode.INVALID_ROLE_ASSIGNMENT,
                HttpStatus.BAD_REQUEST);
    }

    public InvalidRoleAssignmentException(String message) {
        super(message, ErrorCode.INVALID_ROLE_ASSIGNMENT, HttpStatus.BAD_REQUEST);
    }
}
