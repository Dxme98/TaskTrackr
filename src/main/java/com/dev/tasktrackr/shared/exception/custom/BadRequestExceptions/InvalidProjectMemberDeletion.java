package com.dev.tasktrackr.shared.exception.custom.BadRequestExceptions;

import com.dev.tasktrackr.project.domain.enums.PermissionName;
import com.dev.tasktrackr.project.domain.enums.ProjectType;
import com.dev.tasktrackr.shared.exception.ErrorCode;
import com.dev.tasktrackr.shared.exception.custom.AppException;
import org.springframework.http.HttpStatus;

public class InvalidProjectMemberDeletion extends AppException {

    public InvalidProjectMemberDeletion() {
        super("Invalid ProjectMember Deletion: You can't remove our self from Project.",
                ErrorCode.INVALID_PROJECT_MEMBER_DELETION,
                HttpStatus.BAD_REQUEST);
    }
}