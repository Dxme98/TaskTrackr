package com.dev.tasktrackr.shared.exception.custom.NotFoundExceptions;

import com.dev.tasktrackr.shared.exception.ErrorCode;
import com.dev.tasktrackr.shared.exception.custom.AppException;
import org.springframework.http.HttpStatus;

public class UserNotFoundException extends AppException {
    public UserNotFoundException(String userId) {
        super("User with ID: " + userId + " not found", ErrorCode.USER_NOT_FOUND, HttpStatus.NOT_FOUND);
    }
}
