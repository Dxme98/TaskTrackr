package com.dev.tasktrackr.project.service;

import com.dev.tasktrackr.project.api.dtos.ProjectMemberDto;
import com.dev.tasktrackr.project.api.dtos.request.AssignRoleRequest;
import com.dev.tasktrackr.project.api.dtos.request.BasicProjectRoleRequest;
import com.dev.tasktrackr.project.api.dtos.response.ProjectRoleResponse;
import com.dev.tasktrackr.project.domain.ProjectMember;

public interface ProjectRoleService {
    ProjectRoleResponse createProjectRole(String jwtUserId, BasicProjectRoleRequest basicProjectRoleRequest, Long projectId);
    void deleteProjectRole(String jwtUserId, Long projectId, int roleId);
    ProjectMemberDto assignRole(String jwtUserId, AssignRoleRequest assignRoleRequest, Long projectId);
    ProjectRoleResponse renameRole();
}
