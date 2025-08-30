package com.dev.tasktrackr.shared.exception.custom;

import com.dev.tasktrackr.shared.exception.ErrorCode;

public class UnauthorizedInviteHandleAcception extends ForbiddenException{

    public UnauthorizedInviteHandleAcception(String jwtSenderId, String enteredSenderId) {
        super("Authentication mismatch: JWT user ID" + jwtSenderId +  "does not match provided receiverId: " + enteredSenderId,
                ErrorCode.RECEIVER_ID_MISMATCH);
    }
}
