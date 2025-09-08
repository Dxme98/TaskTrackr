package com.dev.tasktrackr.shared.exception.custom.AccessDeniedExceptions;

import com.dev.tasktrackr.shared.exception.ErrorCode;
import com.dev.tasktrackr.shared.exception.custom.AppException;
import org.springframework.http.HttpStatus;

public class PermissionDeniedException extends AppException {
    public PermissionDeniedException(String message) {
        super(message, ErrorCode.MISSING_PERMISSION, HttpStatus.FORBIDDEN);
    }
}
