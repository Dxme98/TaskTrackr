package com.dev.tasktrackr.shared.exception.custom.NotFoundExceptions;

import com.dev.tasktrackr.shared.exception.ErrorCode;
import com.dev.tasktrackr.shared.exception.custom.AppException;
import org.springframework.http.HttpStatus;

public class LinkNotFoundException extends AppException {
    public LinkNotFoundException(Long linkId) {
        super("Link with ID: " + linkId + " not found", ErrorCode.LINK_NOT_FOUND, HttpStatus.NOT_FOUND);
    }
}
