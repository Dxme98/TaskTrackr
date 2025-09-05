package com.dev.tasktrackr.project.service;

import com.dev.tasktrackr.project.api.dtos.ProjectMemberDto;
import com.dev.tasktrackr.project.api.dtos.request.AssignRoleRequest;
import com.dev.tasktrackr.project.api.dtos.request.BasicProjectRoleRequest;
import com.dev.tasktrackr.project.api.dtos.response.ProjectRoleResponse;
import com.dev.tasktrackr.project.domain.Project;
import com.dev.tasktrackr.project.domain.ProjectRole;
import com.dev.tasktrackr.project.repository.ProjectRepository;
import com.dev.tasktrackr.shared.exception.custom.ProjectNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class ProjectRoleServiceImpl implements ProjectRoleService {
    ProjectRepository  projectRepository;


    @Override
    public ProjectRoleResponse createProjectRole(String jwtUserId, BasicProjectRoleRequest basicProjectRoleRequest, Long projectId) {
        Project project = projectRepository.findById(projectId).orElseThrow(() -> new ProjectNotFoundException(projectId));

        project.createRole(basicProjectRoleRequest.getName(), basicProjectRoleRequest.getPermissions());
        ProjectRole createdRole = project.findCreatedRole();
        return null;
    }

    @Override
    public void deleteProjectRole(String jwtUserId, Long projectId, Long roleId) {

    }

    @Override
    public ProjectMemberDto assignRole(String jwtUserId, AssignRoleRequest assignRoleRequest) {
        return null;
    }
}
