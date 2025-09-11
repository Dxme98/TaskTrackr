package com.dev.tasktrackr.project.api.dtos;

import com.dev.tasktrackr.project.domain.enums.PermissionName;
import com.dev.tasktrackr.user.UserDto;
import lombok.*;

import java.util.Set;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ProjectMemberDto {
    private Long id;
    private String userId;
    private String username;
    private String projectId;
    private String role;
    private Set<PermissionName> permissions;
    // Weitere ProjectMember-Eigenschaften (Rolle, joinedAt, etc.)
}
