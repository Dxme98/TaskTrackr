package com.dev.tasktrackr.shared.exception.custom;

import com.dev.tasktrackr.shared.exception.ErrorCode;

public class UserNotFoundException extends ResourceNotFoundException {
    public UserNotFoundException(String userId) {
        super("User with ID: " + userId + " not found", ErrorCode.USER_NOT_FOUND);
    }
}
