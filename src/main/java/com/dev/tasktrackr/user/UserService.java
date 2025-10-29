package com.dev.tasktrackr.user;

import com.dev.tasktrackr.user.domain.UserEntity;

public interface UserService {
    UserEntity findUserById(String userId);
}
