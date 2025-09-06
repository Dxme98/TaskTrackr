package com.dev.tasktrackr.project.service;

import com.dev.tasktrackr.project.api.dtos.ProjectMemberDto;
import com.dev.tasktrackr.project.api.dtos.request.CreateProjectRoleRequest;
import com.dev.tasktrackr.project.api.dtos.response.ProjectRoleResponse;

public interface ProjectRoleService {
    ProjectRoleResponse createProjectRole(String jwtUserId, CreateProjectRoleRequest createProjectRoleRequest, Long projectId);
    void deleteProjectRole(String jwtUserId, Long projectId, int roleId);
    ProjectMemberDto assignRole(String jwtUserId, int roleId, Long memberId, Long projectId);
    ProjectRoleResponse renameRole(String jwtUserId,  String newName,  Long projectId, int roleId);
}
