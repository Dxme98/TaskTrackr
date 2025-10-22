package com.dev.tasktrackr.shared.exception.custom.ConflictExceptions;

import com.dev.tasktrackr.shared.exception.ErrorCode;
import com.dev.tasktrackr.shared.exception.custom.AppException;
import org.springframework.http.HttpStatus;

public class SprintNotActiveException  extends AppException {
    public SprintNotActiveException(Long sprintId) {
        super("Sprint with Name: " + sprintId + " is not active.",
                ErrorCode.SPRINT_NOT_ACTIVE, HttpStatus.CONFLICT);
    }
}
