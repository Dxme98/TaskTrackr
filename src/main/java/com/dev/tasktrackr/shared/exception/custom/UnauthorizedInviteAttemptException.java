package com.dev.tasktrackr.shared.exception.custom;

import com.dev.tasktrackr.shared.exception.ErrorCode;

public class UnauthorizedInviteAttemptException extends ForbiddenException{

    public UnauthorizedInviteAttemptException(String jwtSenderId, String receiverId) {
        super("Authentication mismatch: JWT user ID" + jwtSenderId +  "does not match provided sender ID " + receiverId,
                ErrorCode.SENDER_ID_MISMATCH);
    }
}
