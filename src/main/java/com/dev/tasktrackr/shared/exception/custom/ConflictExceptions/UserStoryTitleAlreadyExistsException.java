package com.dev.tasktrackr.shared.exception.custom.ConflictExceptions;

import com.dev.tasktrackr.shared.exception.ErrorCode;
import com.dev.tasktrackr.shared.exception.custom.AppException;
import org.springframework.http.HttpStatus;

public class UserStoryTitleAlreadyExistsException extends AppException {

    public UserStoryTitleAlreadyExistsException(String userStoryTitle) {
        super("UserStory with Title: " + userStoryTitle + " already exists in Project.", ErrorCode.USERSTORY_TITLE_ALREADY_EXISTS, HttpStatus.CONFLICT);
    }
}