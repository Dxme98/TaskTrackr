package com.dev.tasktrackr.project.api.dtos;

import com.dev.tasktrackr.user.UserDto;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class ProjectMemberDto {
    private Long id;
    private UserDto user;
    // Weitere ProjectMember-Eigenschaften (Rolle, joinedAt, etc.)
}
