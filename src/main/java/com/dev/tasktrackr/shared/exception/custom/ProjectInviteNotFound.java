package com.dev.tasktrackr.shared.exception.custom;

import com.dev.tasktrackr.shared.exception.ErrorCode;
import org.springframework.http.HttpStatus;

public class ProjectInviteNotFound extends AppException{

    public ProjectInviteNotFound(Long inviteId) {
        super("Invite with ID: " + inviteId + " not found", ErrorCode.INVITE_NOT_FOUND, HttpStatus.NOT_FOUND);
    }
}
