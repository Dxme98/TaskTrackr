package com.dev.tasktrackr.shared.exception.custom.ConflictExceptions;

import com.dev.tasktrackr.shared.exception.ErrorCode;
import com.dev.tasktrackr.shared.exception.custom.AppException;
import org.springframework.http.HttpStatus;

public class RoleNameAlreadyExistsException  extends AppException {
    public RoleNameAlreadyExistsException(String roleName) {
        super("Role with name: " + roleName + " already exists",
                ErrorCode.ROLE_NAME_ALREADY_EXISTS, HttpStatus.CONFLICT);
    }
}
