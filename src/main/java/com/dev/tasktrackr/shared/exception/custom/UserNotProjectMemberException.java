package com.dev.tasktrackr.shared.exception.custom;

import com.dev.tasktrackr.shared.exception.ErrorCode;

public class UserNotProjectMemberException extends  ForbiddenException{
    public UserNotProjectMemberException(String userId) {
        super("User with ID " + userId + "   is not part of Project.", ErrorCode.USER_NOT_PART_OF_PROJECT);
    }
}
