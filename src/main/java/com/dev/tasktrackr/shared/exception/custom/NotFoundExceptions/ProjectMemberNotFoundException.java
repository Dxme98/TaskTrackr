package com.dev.tasktrackr.shared.exception.custom.NotFoundExceptions;

import com.dev.tasktrackr.shared.exception.ErrorCode;
import com.dev.tasktrackr.shared.exception.custom.AppException;
import org.springframework.http.HttpStatus;

import java.util.Set;

public class ProjectMemberNotFoundException extends AppException {
    public ProjectMemberNotFoundException(Long projectMemberId) {
        super("Projectmember with ID: " + projectMemberId + " not found.", ErrorCode.PROJECT_MEMBER_NOT_FOUND, HttpStatus.NOT_FOUND);
    }

    public ProjectMemberNotFoundException(String message) {
        super(message, ErrorCode.PROJECT_MEMBER_NOT_FOUND, HttpStatus.NOT_FOUND);
    }

    public ProjectMemberNotFoundException(Set<Long> projectMemberIds) {
        super("Projectmember with IDs: " + projectMemberIds + " not found.", ErrorCode.PROJECT_MEMBER_NOT_FOUND, HttpStatus.NOT_FOUND);
    }
}
