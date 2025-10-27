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
import com.dev.tasktrackr.project.domain.enums.RoleType;
import com.dev.tasktrackr.project.repository.ProjectMemberQueryRepository;
import com.dev.tasktrackr.project.repository.ProjectRepository;
import com.dev.tasktrackr.project.repository.ProjectRoleQueryRepository;
import com.dev.tasktrackr.shared.exception.custom.BadRequestExceptions.InvalidRoleAssignmentException;
import com.dev.tasktrackr.shared.exception.custom.BadRequestExceptions.InvalidRoleDeletion;
import com.dev.tasktrackr.shared.exception.custom.ConflictExceptions.RoleNameAlreadyExistsException;
import com.dev.tasktrackr.shared.exception.custom.NotFoundExceptions.ProjectNotFoundException;
import com.dev.tasktrackr.shared.exception.custom.NotFoundExceptions.RoleNotFoundException;
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
    private final ProjectAccessService projectAccessService;
    private final ProjectMemberQueryRepository projectMemberQueryRepository;

    @Override
    @Transactional
    public ProjectRoleResponse createProjectRole(String jwtUserId, CreateProjectRoleRequest createProjectRoleRequest, Long projectId) {
        Project project = projectAccessService.findProjectByIdWithRoles(projectId);
        ProjectMember member = projectAccessService.findProjectMemberWithPermissionsRolesAndUser(jwtUserId, projectId);

        member.canManageRoles();

        checkForUniqueRoleName(createProjectRoleRequest.getName(), projectId);

        ProjectRole role  = ProjectRole.createCustomRole(project, createProjectRoleRequest.getName(), createProjectRoleRequest.getPermissions());
        projectRoleQueryRepository.save(role);

        var event = new RoleCreatedEvent(projectId, member.getId(), member.getUser().getUsername(), (long) role.getId(), role.getName() );
        applicationEventPublisher.publishEvent(event);

        return roleMapper.toResponse(role);
    }

    @Override
    @Transactional
    public void deleteProjectRole(String jwtUserId, Long projectId, int roleId) {
        ProjectMember member = projectAccessService.findProjectMemberWithPermissionsRolesAndUser(jwtUserId, projectId);
        ProjectRole roleToDelete = findRoleById(roleId);

        member.canManageRoles();

        // checks
        validateRoleDeletion(roleToDelete);

        projectRoleQueryRepository.delete(roleToDelete);

        var event = new RoleDeletedEvent(projectId, member.getId(), member.getUser().getUsername(), (long) roleToDelete.getId(), roleToDelete.getName());
        applicationEventPublisher.publishEvent(event);
    }

    @Override
    @Transactional
    public ProjectMemberDto assignRole(String jwtUserId, int roleId, Long memberId, Long projectId) {
        ProjectMember member = projectAccessService.findProjectMemberWithPermissionsRolesAndUser(jwtUserId, projectId);
        ProjectMember memberToAssignRole =  projectAccessService.findProjectMemberWithPermissionsRolesAndUser(memberId, projectId);
        ProjectRole roleToAssign = findRoleById(roleId);

        member.canManageRoles();

        validateRoleAssignment(roleToAssign,  memberToAssignRole, member,jwtUserId, projectId);

        memberToAssignRole.assignRole(roleToAssign);


        var event = new RoleAssignedEvent(projectId, member.getId(), member.getUser().getUsername(), (long) roleId, memberToAssignRole.getUser().getUsername(), memberToAssignRole.getProjectRole().getName() );
        applicationEventPublisher.publishEvent(event);

        return projectMemberMapper.toResponse(memberToAssignRole);
    }

    @Override
    @Transactional
    public ProjectRoleResponse renameRole(String jwtUserId,  String newName,  Long projectId, int roleId) {
        ProjectMember member = projectAccessService.findProjectMemberWithPermissionsRolesAndUser(jwtUserId, projectId);

        member.canManageRoles();


        ProjectRole role = projectRoleQueryRepository.findById(roleId).orElseThrow(() -> new RoleNotFoundException(roleId));
        checkForUniqueRoleName(newName, projectId);

        role.renameRole(newName);

        return roleMapper.toResponse(role);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ProjectRoleResponse> getAllRoles(String jwtUserId, Pageable pageable, Long projectId) {
        projectAccessService.checkProjectMemberShip(projectId, jwtUserId);

        Page<ProjectRole> roles = projectRoleQueryRepository.findAllByProjectId(projectId, pageable);

        return roles.map(roleMapper::toResponse);
    }


    /** Validation */
    void validateRoleDeletion(ProjectRole roleToDelete) {
        roleToDelete.canBeDeleted();
        if(projectMemberQueryRepository.existsByProjectRoleId(roleToDelete.getId())) throw new InvalidRoleDeletion("Remove Role from ProjectMember before deleting the Role");; // Role can only be deleted if no user is assigned to it
    }

    void validateRoleAssignment(ProjectRole roleToAssign, ProjectMember memberToAssignRole, ProjectMember member, String jwtUserId, Long projectId) {
        ProjectRole currentRole = memberToAssignRole.getProjectRole();

        // 3.
        if (roleToAssign.getRoleType() == RoleType.OWNER &&
                memberToAssignRole.getUser().getId().equals(jwtUserId) && // Ziel ist gleich Akteur
                member.getProjectRole().getRoleType() != RoleType.OWNER) { // Akteur ist kein Owner

            throw new InvalidRoleAssignmentException("You cannot assign yourself to OWNER role");
        }

        if (!currentRole.equals(roleToAssign)) {

            if (currentRole.getRoleType() == RoleType.OWNER && roleToAssign.getRoleType() != RoleType.OWNER) {

                long ownerCount = projectMemberQueryRepository.countByProjectIdAndProjectRole_RoleType(projectId, RoleType.OWNER);

                if (ownerCount <= 1) {
                    throw new InvalidRoleAssignmentException("At least one OWNER must exist in project");
                }
            }
        }
    }

    /** Helper */

    void checkForUniqueRoleName(String name, Long projectId) {
        if(projectRoleQueryRepository.existsByNameAndProjectId(name, projectId)) {
            throw new RoleNameAlreadyExistsException(name);
        }
    }

    ProjectRole findRoleById(int roleId) {
       return projectRoleQueryRepository.findById(roleId).orElseThrow(() -> new RoleNotFoundException(roleId));
    }
}
