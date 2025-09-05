package com.dev.tasktrackr.project.service;

import com.dev.tasktrackr.project.api.dtos.ProjectMemberDto;
import com.dev.tasktrackr.project.api.dtos.ProjectMemberMapper;
import com.dev.tasktrackr.project.api.dtos.RoleMapper;
import com.dev.tasktrackr.project.api.dtos.request.AssignRoleRequest;
import com.dev.tasktrackr.project.api.dtos.request.BasicProjectRoleRequest;
import com.dev.tasktrackr.project.api.dtos.response.ProjectRoleResponse;
import com.dev.tasktrackr.project.domain.Project;
import com.dev.tasktrackr.project.domain.ProjectMember;
import com.dev.tasktrackr.project.domain.ProjectRole;
import com.dev.tasktrackr.project.repository.ProjectRepository;
import com.dev.tasktrackr.shared.exception.custom.ProjectNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class ProjectRoleServiceImpl implements ProjectRoleService {
    private final ProjectRepository  projectRepository;
    private final RoleMapper roleMapper;
    private final ProjectMemberMapper projectMemberMapper;

    // VALIDATION MISSING, FETCH STRATEGIEN, GGF. endpoint anpassung (projectId als parameter)=

    @Override
    public ProjectRoleResponse createProjectRole(String jwtUserId, BasicProjectRoleRequest basicProjectRoleRequest, Long projectId) {
        Project project = projectRepository.findById(projectId).orElseThrow(() -> new ProjectNotFoundException(projectId));

        project.createRole(basicProjectRoleRequest.getName(), basicProjectRoleRequest.getPermissions());
        ProjectRole persistedRole = project.getProjectRoles().get(project.getProjectRoles().size()-1);

        return roleMapper.toResponse(persistedRole);
    }

    @Override
    public void deleteProjectRole(String jwtUserId, Long projectId, int roleId) {
        Project project = projectRepository.findById(projectId).orElseThrow(() -> new ProjectNotFoundException(projectId));
        project.deleteRole(roleId);

        projectRepository.save(project);
    }

    @Override
    public ProjectMemberDto assignRole(String jwtUserId, AssignRoleRequest assignRoleRequest, Long projectId) {
        Project project = projectRepository.findById(projectId).orElseThrow(() -> new ProjectNotFoundException(projectId));

        ProjectMember updatedMember = project.assignRole(assignRoleRequest.getRoleId(), assignRoleRequest.getProjectMemberId());

        projectRepository.save(project);

        return projectMemberMapper.toResponse(updatedMember);
    }
}
