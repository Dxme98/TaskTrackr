package com.dev.tasktrackr.user;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class UserDto {
    private String id;
    private String username;
    private LocalDateTime createdAt;
}
