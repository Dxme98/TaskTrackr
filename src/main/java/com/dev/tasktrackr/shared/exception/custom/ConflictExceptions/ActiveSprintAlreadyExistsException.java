package com.dev.tasktrackr.shared.exception.custom.ConflictExceptions;

import com.dev.tasktrackr.shared.exception.ErrorCode;
import com.dev.tasktrackr.shared.exception.custom.AppException;
import org.springframework.http.HttpStatus;

public class ActiveSprintAlreadyExistsException extends AppException {

    public ActiveSprintAlreadyExistsException() {
        super("Es kann nur ein Sprint gleichzeitig aktiv sein.", ErrorCode.ACTIVE_SPRINT_ALREADY_EXISTS, HttpStatus.CONFLICT);
    }
}
