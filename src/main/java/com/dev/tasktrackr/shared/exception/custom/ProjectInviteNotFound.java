package com.dev.tasktrackr.shared.exception.custom;

import com.dev.tasktrackr.shared.exception.ErrorCode;

public class ProjectInviteNotFound extends ResourceNotFoundException{

    public ProjectInviteNotFound(Long inviteId) {
        super("Invite with ID: " + inviteId + " not found", ErrorCode.INVITE_NOT_FOUND);
    }
}
