package com.dev.tasktrackr.shared.exception.custom.NotFoundExceptions;

import com.dev.tasktrackr.shared.exception.ErrorCode;
import com.dev.tasktrackr.shared.exception.custom.AppException;
import org.springframework.http.HttpStatus;

public class TaskNotFoundException extends AppException {
    public TaskNotFoundException(Long taskId) {
        super("Task with ID: " +  taskId + " not found", ErrorCode.TASK_NOT_FOUND, HttpStatus.NOT_FOUND);
    }
}
