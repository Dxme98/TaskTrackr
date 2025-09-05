package com.dev.tasktrackr.shared.exception.custom;

import com.dev.tasktrackr.shared.exception.ErrorCode;
import org.springframework.http.HttpStatus;

public class UserAlreadyPartOfProjectException extends AppException{
    public UserAlreadyPartOfProjectException(String receiverId, Long projectId) {
        super("User with ID " + receiverId + " is already part of Project with ID: " + projectId,
                ErrorCode.USER_IS_ALREADY_PART_OF_PROJECT, HttpStatus.CONFLICT);
    }
}
