package com.dev.tasktrackr.shared.exception.custom.NotFoundExceptions;

import com.dev.tasktrackr.shared.exception.ErrorCode;
import com.dev.tasktrackr.shared.exception.custom.AppException;
import org.springframework.http.HttpStatus;

public class CommentNotFoundException extends AppException {
    public CommentNotFoundException(Long commentId) {
        super("Comment with ID: " + commentId + " not found", ErrorCode.COMMENT_NOT_FOUND, HttpStatus.NOT_FOUND);
    }
}
