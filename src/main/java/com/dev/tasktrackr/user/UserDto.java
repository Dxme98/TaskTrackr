package com.dev.tasktrackr.user;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
public class UserDto {
    private String id;
    private String username;
}
