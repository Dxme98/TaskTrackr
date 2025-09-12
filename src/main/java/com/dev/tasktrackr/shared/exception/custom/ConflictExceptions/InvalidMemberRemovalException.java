package com.dev.tasktrackr.shared.exception.custom.ConflictExceptions;

import com.dev.tasktrackr.shared.exception.ErrorCode;
import com.dev.tasktrackr.shared.exception.custom.AppException;
import org.springframework.http.HttpStatus;

public class InvalidMemberRemovalException extends AppException {

    public InvalidMemberRemovalException(String message) {
        super(message, ErrorCode.INVALID_MEMBER_REMOVAL, HttpStatus.CONFLICT);
    }
}
