package com.dev.tasktrackr.shared.exception.custom;

import com.dev.tasktrackr.shared.exception.ErrorCode;
import org.springframework.http.HttpStatus;

public class ProjectInviteAlreadyExistsException extends AppException{
    public ProjectInviteAlreadyExistsException(String receiverId, Long projectId) {
        super("User with ID " + receiverId + " already got a invite for project: " + projectId,
                ErrorCode.INVITE_ALREADY_EXISTS,
                HttpStatus.CONFLICT);
    }
}
