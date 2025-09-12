package com.dev.tasktrackr.shared.exception.custom.AccessDeniedExceptions;

import com.dev.tasktrackr.shared.exception.ErrorCode;
import com.dev.tasktrackr.shared.exception.custom.AppException;
import org.springframework.http.HttpStatus;

public class UnauthorizedInviteHandleAcception extends AppException {

    public UnauthorizedInviteHandleAcception(String jwtSenderId, String enteredSenderId) {
        super("Authentication mismatch: JWT user ID" + jwtSenderId +  "does not match provided receiverId: " + enteredSenderId,
                ErrorCode.RECEIVER_ID_MISMATCH, HttpStatus.FORBIDDEN);
    }
}
