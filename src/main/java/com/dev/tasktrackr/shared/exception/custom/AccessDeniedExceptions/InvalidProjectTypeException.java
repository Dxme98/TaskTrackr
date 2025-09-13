package com.dev.tasktrackr.shared.exception.custom.AccessDeniedExceptions;

import com.dev.tasktrackr.shared.exception.ErrorCode;
import com.dev.tasktrackr.shared.exception.custom.AppException;
import org.springframework.http.HttpStatus;

public class InvalidProjectTypeException extends AppException {
    public InvalidProjectTypeException(String message) {
        super(message, ErrorCode.INVALID_PROJECT_TYPE, HttpStatus.FORBIDDEN);
    }
}
