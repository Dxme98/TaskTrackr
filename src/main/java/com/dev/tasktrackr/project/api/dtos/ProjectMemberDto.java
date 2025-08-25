package com.dev.tasktrackr.project.api.dtos;

import com.dev.tasktrackr.user.UserDto;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ProjectMemberDto {
    private UserDto user;
    // Weitere ProjectMember-Eigenschaften (Rolle, joinedAt, etc.)
}
