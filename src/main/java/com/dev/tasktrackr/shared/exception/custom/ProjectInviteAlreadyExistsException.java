package com.dev.tasktrackr.shared.exception.custom;

import com.dev.tasktrackr.shared.exception.ErrorCode;

public class ProjectInviteAlreadyExistsException extends ConflictException{
    public ProjectInviteAlreadyExistsException(String receiverId, Long projectId) {
        super("User with ID " + receiverId + " already got a invite for project: " + projectId,
                ErrorCode.INVITE_ALREADY_EXISTS);
    }
}
