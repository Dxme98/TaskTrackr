package com.dev.tasktrackr.shared.exception.custom.NotFoundExceptions;

import com.dev.tasktrackr.shared.exception.ErrorCode;
import com.dev.tasktrackr.shared.exception.custom.AppException;
import org.springframework.http.HttpStatus;

public class NoActiveSprintFoundException extends AppException {
    public NoActiveSprintFoundException(Long projectId) {
        super("No active Sprint found for Project with ID " + projectId, ErrorCode.NO_ACTIVE_SPRINT_FOUND, HttpStatus.NOT_FOUND);
    }}
