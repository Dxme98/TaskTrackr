package com.dev.tasktrackr.shared.exception.custom;

import com.dev.tasktrackr.shared.exception.ErrorCode;
import org.springframework.http.HttpStatus;

public class InvalidRoleDeletion extends AppException{
    public InvalidRoleDeletion(String message) {
        super(message, ErrorCode.INVALID_ROLE_DELETION, HttpStatus.BAD_REQUEST);
    }
}
