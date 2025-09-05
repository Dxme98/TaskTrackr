package com.dev.tasktrackr.shared.exception.custom;

import com.dev.tasktrackr.shared.exception.ErrorCode;
import org.springframework.http.HttpStatus;

public class RoleNotFoundException extends AppException {
    public RoleNotFoundException(int roleId) {
        super("Role with ID: " + roleId + "not found", ErrorCode.ROLE_NOT_FOUND, HttpStatus.NOT_FOUND );
    }
}
