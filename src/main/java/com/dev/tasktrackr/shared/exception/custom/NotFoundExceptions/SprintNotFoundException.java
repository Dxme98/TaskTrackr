package com.dev.tasktrackr.shared.exception.custom.NotFoundExceptions;

import com.dev.tasktrackr.shared.exception.ErrorCode;
import com.dev.tasktrackr.shared.exception.custom.AppException;
import org.springframework.http.HttpStatus;

public class SprintNotFoundException extends AppException {
    public SprintNotFoundException(Long sprintId) {
        super("Sprint with ID: " + sprintId + "not found", ErrorCode.SPRINT_NOT_FOUND, HttpStatus.NOT_FOUND );
    }
}
