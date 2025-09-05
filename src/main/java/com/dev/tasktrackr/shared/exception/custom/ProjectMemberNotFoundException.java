package com.dev.tasktrackr.shared.exception.custom;

import com.dev.tasktrackr.shared.exception.ErrorCode;
import org.springframework.http.HttpStatus;

public class ProjectMemberNotFoundException extends AppException{
    public ProjectMemberNotFoundException(Long projectMemberId) {
        super("Projectmember with ID: " + projectMemberId + " not found.", ErrorCode.PROJECT_MEMBER_NOT_FOUND, HttpStatus.NOT_FOUND);
    }
}
