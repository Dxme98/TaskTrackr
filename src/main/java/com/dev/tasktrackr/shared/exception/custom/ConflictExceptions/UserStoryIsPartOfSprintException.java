package com.dev.tasktrackr.shared.exception.custom.ConflictExceptions;


import com.dev.tasktrackr.shared.exception.ErrorCode;
import com.dev.tasktrackr.shared.exception.custom.AppException;
import org.springframework.http.HttpStatus;

public class UserStoryIsPartOfSprintException extends AppException {

    public UserStoryIsPartOfSprintException(Long userStoryId) {
        super("Remove UserStory with ID: " + userStoryId + " from Sprint to delete it", ErrorCode.INVALID_USERSTORY_DELETION, HttpStatus.CONFLICT);
    }
}
