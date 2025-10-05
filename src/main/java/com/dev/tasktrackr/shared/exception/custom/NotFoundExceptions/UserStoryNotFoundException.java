package com.dev.tasktrackr.shared.exception.custom.NotFoundExceptions;

import com.dev.tasktrackr.shared.exception.ErrorCode;
import com.dev.tasktrackr.shared.exception.custom.AppException;
import org.springframework.http.HttpStatus;

public class UserStoryNotFoundException extends AppException {
    public UserStoryNotFoundException(String title) {
        super("Userstory with Title: " + title + " not found", ErrorCode.USERSTORY_NOT_FOUND, HttpStatus.NOT_FOUND);
    }
}
