package com.dev.tasktrackr.shared.exception.custom.NotFoundExceptions;

import com.dev.tasktrackr.shared.exception.ErrorCode;
import com.dev.tasktrackr.shared.exception.custom.AppException;
import org.springframework.http.HttpStatus;

public class SprintSummaryItemNotFoundException extends AppException {
    public SprintSummaryItemNotFoundException(Long userStoryId) {
        super("SprintSummaryItem with UserStoryID : " +  userStoryId + " not found", ErrorCode.SPRINT_SUMMARY_ITEM_NOT_FOUND, HttpStatus.NOT_FOUND);
    }
}
