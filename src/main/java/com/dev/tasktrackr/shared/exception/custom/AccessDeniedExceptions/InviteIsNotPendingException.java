package com.dev.tasktrackr.shared.exception.custom.AccessDeniedExceptions;

import com.dev.tasktrackr.shared.exception.ErrorCode;
import com.dev.tasktrackr.shared.exception.custom.AppException;
import org.springframework.http.HttpStatus;

public class InviteIsNotPendingException extends AppException {

    public InviteIsNotPendingException(Long inviteId) {
        super("Invite id " + inviteId + " is not PENDING.",
                ErrorCode.INVITE_NOT_PENDING,
                HttpStatus.FORBIDDEN);
    }
}
