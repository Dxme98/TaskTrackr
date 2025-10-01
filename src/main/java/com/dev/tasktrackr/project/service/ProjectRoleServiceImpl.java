package com.dev.tasktrackr.project.service;

import static com.dev.tasktrackr.activity.ProjectActivityEvents.RoleCreatedEvent;
import static com.dev.tasktrackr.activity.ProjectActivityEvents.RoleDeletedEvent;
import static com.dev.tasktrackr.activity.ProjectActivityEvents.RoleAssignedEvent;

import com.dev.tasktrackr.project.api.dtos.response.ProjectMemberDto;
import com.dev.tasktrackr.project.api.dtos.mapper.ProjectMemberMapper;
import com.dev.tasktrackr.project.api.dtos.mapper.RoleMapper;
import com.dev.tasktrackr.project.api.dtos.request.CreateProjectRoleRequest;
import com.dev.tasktrackr.project.api.dtos.response.ProjectRoleResponse;
import com.dev.tasktrackr.project.domain.Project;
import com.dev.tasktrackr.project.domain.ProjectMember;
import com.dev.tasktrackr.project.domain.ProjectRole;
import com.dev.tasktrackr.project.repository.ProjectRepository;
import com.dev.tasktrackr.project.repository.ProjectRoleQueryRepository;
import com.dev.tasktrackr.shared.exception.custom.NotFoundExceptions.ProjectNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
@Slf4j
public class ProjectRoleServiceImpl implements ProjectRoleService {
    private final ProjectRepository  projectRepository;
    private final RoleMapper roleMapper;
    private final ProjectMemberMapper projectMemberMapper;
    private final ProjectRoleQueryRepository projectRoleQueryRepository;
    private final ApplicationEventPublisher applicationEventPublisher;

    @Override
    @Transactional
    public ProjectRoleResponse createProjectRole(String jwtUserId, CreateProjectRoleRequest createProjectRoleRequest, Long projectId) {
        Project project = findProjectWithAttributes(projectId);
        memberCanManageRoles(project, jwtUserId);
        ProjectMember actingMember = project.findProjectMember(jwtUserId);

        project.createRole(createProjectRoleRequest.getName(), createProjectRoleRequest.getPermissions());

        projectRepository.save(project);

        ProjectRole persistedRole = project.getProjectRoles().get(project.getProjectRoles().size()-1);

        log.info("ROLLEN ID GENIERTE {}", persistedRole.getId());

        var event = new RoleCreatedEvent(projectId, actingMember.getId(), actingMember.getUser().getUsername(), (long) persistedRole.getId(), persistedRole.getName() );
        applicationEventPublisher.publishEvent(event);

        return roleMapper.toResponse(persistedRole);
    }

    @Override
    @Transactional
    public void deleteProjectRole(String jwtUserId, Long projectId, int roleId) {
        Project project = findProjectWithAttributes(projectId);
        memberCanManageRoles(project, jwtUserId);
        ProjectMember actingMember = project.findProjectMember(jwtUserId);

        ProjectRole deletedRole = project.deleteRole(roleId);

        var event = new RoleDeletedEvent(projectId, actingMember.getId(), actingMember.getUser().getUsername(), (long) deletedRole.getId(), deletedRole.getName() );
        applicationEventPublisher.publishEvent(event);

        projectRepository.save(project);
    }

    @Override
    @Transactional
    public ProjectMemberDto assignRole(String jwtUserId, int roleId, Long memberId, Long projectId) {
        Project project = findProjectWithAttributes(projectId);
        memberCanManageRoles(project, jwtUserId);
        ProjectMember actingMember = project.findProjectMember(jwtUserId);

        ProjectMember updatedMember = project.assignRole(roleId, memberId, jwtUserId);

        projectRepository.save(project);

        var event = new RoleAssignedEvent(projectId, actingMember.getId(), actingMember.getUser().getUsername(), (long) roleId, updatedMember.getUser().getUsername(), updatedMember.getProjectRole().getName() );
        applicationEventPublisher.publishEvent(event);

        return projectMemberMapper.toResponse(updatedMember);
    }

    @Override
    @Transactional
    public ProjectRoleResponse renameRole(String jwtUserId,  String newName,  Long projectId, int roleId) {
        Project project = findProjectWithAttributes(projectId);
        memberCanManageRoles(project, jwtUserId);

        ProjectRole updatedRole = project.renameRole(roleId, newName);

        projectRepository.save(project);

        return roleMapper.toResponse(updatedRole);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ProjectRoleResponse> getAllRoles(String jwtUserId, Pageable pageable, Long projectId) {
        Project project = projectRepository.findProjectWithInvitesAndMember(projectId)
                .orElseThrow(() -> new ProjectNotFoundException(projectId));
        project.findProjectMember(jwtUserId); // checks if user is part of projects


        Page<ProjectRole> roles = projectRoleQueryRepository.findAllByProjectId(projectId, pageable);

        return roles.map(roleMapper::toResponse);
    }

    private Project findProjectWithAttributes(Long projectId) {
        return projectRepository.findProjectWithRolesAndPermissions(projectId).orElseThrow(() -> new ProjectNotFoundException(projectId));
    }

    private void memberCanManageRoles(Project project, String jwtUserId) {
        ProjectMember member = project.findProjectMember(jwtUserId);
        member.canManageRoles();
    }
}
