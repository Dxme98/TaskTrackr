package com.dev.tasktrackr.shared.exception.custom.ConflictExceptions;

import com.dev.tasktrackr.shared.exception.ErrorCode;
import com.dev.tasktrackr.shared.exception.custom.AppException;
import org.springframework.http.HttpStatus;

public class LinkTitleAlreadyExistsException extends AppException {
    public LinkTitleAlreadyExistsException(String title) {
        super("Link with title: " + title + " already exists ",
                ErrorCode.LINK_TITLE_ALREADY_EXISTS,
                HttpStatus.CONFLICT);
    }
}
