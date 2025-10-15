package com.dev.tasktrackr.shared.exception.custom.NotFoundExceptions;

import com.dev.tasktrackr.shared.exception.ErrorCode;
import com.dev.tasktrackr.shared.exception.custom.AppException;
import org.springframework.http.HttpStatus;

public class SprintBacklogItemNotFoundException extends AppException {
    public SprintBacklogItemNotFoundException(Long id) {
        super("SprintBacklogItem with ID : " +  id + " not found", ErrorCode.SPRINT_BACKLOG_ITEM_NOT_FOUND, HttpStatus.NOT_FOUND);
    }
}

