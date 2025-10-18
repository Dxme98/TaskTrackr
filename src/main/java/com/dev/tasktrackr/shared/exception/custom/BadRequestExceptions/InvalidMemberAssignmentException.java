package com.dev.tasktrackr.shared.exception.custom.BadRequestExceptions;



import com.dev.tasktrackr.project.domain.enums.PermissionName;
import com.dev.tasktrackr.project.domain.enums.ProjectType;
import com.dev.tasktrackr.shared.exception.ErrorCode;
import com.dev.tasktrackr.shared.exception.custom.AppException;
import org.springframework.http.HttpStatus;

public class InvalidMemberAssignmentException extends AppException {

    public InvalidMemberAssignmentException() {
        super("UserStory assignment can only be done while Task is in SPRINT BACKLOG",
                ErrorCode.INVALID_MEMBER_ASSIGNMENT,
                HttpStatus.BAD_REQUEST);
    }
}
