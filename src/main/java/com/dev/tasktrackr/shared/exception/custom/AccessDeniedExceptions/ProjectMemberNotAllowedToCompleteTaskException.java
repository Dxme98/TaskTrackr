package com.dev.tasktrackr.shared.exception.custom.AccessDeniedExceptions;

import com.dev.tasktrackr.shared.exception.ErrorCode;
import com.dev.tasktrackr.shared.exception.custom.AppException;
import org.springframework.http.HttpStatus;

public class ProjectMemberNotAllowedToCompleteTaskException extends AppException {
    public ProjectMemberNotAllowedToCompleteTaskException(Long memberId) {
        super("Projectmember with ID: " + memberId + " is not allowed to complete task", ErrorCode.MEMBER_NOT_ALLOWED_TO_COMPLETE_TASK, HttpStatus.FORBIDDEN);
    }
}
