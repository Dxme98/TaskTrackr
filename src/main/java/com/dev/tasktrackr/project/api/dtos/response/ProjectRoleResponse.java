package com.dev.tasktrackr.project.api.dtos.response;

import com.dev.tasktrackr.project.domain.enums.PermissionName;
import lombok.Getter;
import lombok.Setter;

import java.util.Set;

@Getter
@Setter
public class ProjectRoleResponse {
    private int id;
    private String name;
    private Long projectId;
    Set<PermissionName> permissions;
}
