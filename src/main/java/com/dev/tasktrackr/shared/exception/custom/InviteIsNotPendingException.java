package com.dev.tasktrackr.shared.exception.custom;

import com.dev.tasktrackr.shared.exception.ErrorCode;

public class InviteIsNotPendingException extends ForbiddenException{

    public InviteIsNotPendingException(Long inviteId) {
        super("Invite id " + inviteId + " is not PENDING.",
                ErrorCode.INVITE_NOT_PENDING);
    }
}
