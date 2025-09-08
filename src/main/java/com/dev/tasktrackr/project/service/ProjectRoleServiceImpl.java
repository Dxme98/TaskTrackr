package com.dev.tasktrackr.project.service;

import com.dev.tasktrackr.project.api.dtos.ProjectMemberDto;
import com.dev.tasktrackr.project.api.dtos.ProjectMemberMapper;
import com.dev.tasktrackr.project.api.dtos.RoleMapper;
import com.dev.tasktrackr.project.api.dtos.request.CreateProjectRoleRequest;
import com.dev.tasktrackr.project.api.dtos.response.ProjectRoleResponse;
import com.dev.tasktrackr.project.domain.Project;
import com.dev.tasktrackr.project.domain.ProjectMember;
import com.dev.tasktrackr.project.domain.ProjectRole;
import com.dev.tasktrackr.project.repository.ProjectRepository;
import com.dev.tasktrackr.project.repository.ProjectRoleQueryRepository;
import com.dev.tasktrackr.shared.exception.custom.ProjectNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

@RequiredArgsConstructor
@Service
@Slf4j
public class ProjectRoleServiceImpl implements ProjectRoleService {
    private final ProjectRepository  projectRepository;
    private final RoleMapper roleMapper;
    private final ProjectMemberMapper projectMemberMapper;
    private final ProjectRoleQueryRepository projectRoleQueryRepository;

    // VALIDATION MISSING, FETCH STRATEGIEN, GGF. endpoint anpassung (projectId als parameter)=

    @Override
    @Transactional
    public ProjectRoleResponse createProjectRole(String jwtUserId, CreateProjectRoleRequest createProjectRoleRequest, Long projectId) {
        Project project = findProjectWithAttributes(projectId);

        project.createRole(createProjectRoleRequest.getName(), createProjectRoleRequest.getPermissions());

        projectRepository.save(project);

        ProjectRole persistedRole = project.getProjectRoles().get(project.getProjectRoles().size()-1);

        log.info("ROLLEN ID GENIERTE {}", persistedRole.getId());

        return roleMapper.toResponse(persistedRole);
    }

    @Override
    @Transactional
    public void deleteProjectRole(String jwtUserId, Long projectId, int roleId) {
        Project project = findProjectWithAttributes(projectId);
        project.deleteRole(roleId);

        projectRepository.save(project);
    }

    @Override
    @Transactional
    public ProjectMemberDto assignRole(String jwtUserId, int roleId, Long memberId, Long projectId) {
        Project project = findProjectWithAttributes(projectId);

        ProjectMember updatedMember = project.assignRole(roleId, memberId, jwtUserId);

        projectRepository.save(project);

        return projectMemberMapper.toResponse(updatedMember);
    }

    @Override
    @Transactional
    public ProjectRoleResponse renameRole(String jwtUserId,  String newName,  Long projectId, int roleId) {
        Project project = findProjectWithAttributes(projectId);

        ProjectRole updatedRole = project.renameRole(roleId, newName);

        projectRepository.save(project);

        return roleMapper.toResponse(updatedRole);
    }

    @Override
    public Page<ProjectRoleResponse> getAllRoles(String jwtUserId, Pageable pageable, Long projectId) {
        Page<ProjectRole> roles = projectRoleQueryRepository.findAllByProjectId(projectId, pageable);

        return roles.map(roleMapper::toResponse);
    }

    public Project findProjectWithAttributes(Long projectId) {
        return projectRepository.findProjectWithRolesAndPermissions(projectId).orElseThrow(() -> new ProjectNotFoundException(projectId));
    }
}
