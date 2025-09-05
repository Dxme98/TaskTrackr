package com.dev.tasktrackr.project.domain.validator;

import com.dev.tasktrackr.project.domain.Project;
import com.dev.tasktrackr.project.domain.ProjectMember;
import com.dev.tasktrackr.project.domain.ProjectRole;
import com.dev.tasktrackr.project.domain.enums.RoleType;
import com.dev.tasktrackr.shared.exception.custom.*;

public class ProjectValidator {

    public static void validateAddMember(Project project, String userId) {
        // Prüfen ob User bereits Mitglied ist
        boolean isAlreadyMember = project.getProjectMembers().stream()
                .anyMatch(m -> m.getUser().getId().equals(userId));
        if (isAlreadyMember) {
            throw new UserAlreadyPartOfProjectException(userId, project.getId());
        }
    }

    public static void validateInviteCreation(Project project, String receiverId, String senderId) {
        // Prüfen ob Einladung bereits existiert
        boolean inviteExists = project.getProjectInvites().stream()
                .anyMatch(invite -> invite.getReceiver().getId().equals(receiverId));
        if (inviteExists) throw new ProjectInviteAlreadyExistsException(receiverId, project.getId());

        // Prüfen ob User bereits Teil des Projekts ist
        boolean receiverIsMember = project.getProjectMembers().stream()
                .anyMatch(member -> member.getUser().getId().equals(receiverId));
        if (receiverIsMember) throw new UserAlreadyPartOfProjectException(receiverId, project.getId());

        // Prüfen ob Sender Mitglied des Projekts ist
        boolean senderIsMember = project.getProjectMembers().stream()
                .anyMatch(member -> member.getUser().getId().equals(senderId));
        if (!senderIsMember) throw new UserNotProjectMemberException(senderId);
    }

    public static void validateRoleCreation(Project project, String roleName) {
        boolean nameExists = project.getProjectRoles().stream()
                .anyMatch(r -> r.getName().equalsIgnoreCase(roleName));
        if(nameExists) throw new RoleNameAlreadyExistsException(roleName);
    }

    public static void validateRoleDeletion(Project project,int roleId) {
        ProjectRole role = project.getProjectRoles().stream()
                .filter(r -> r.getId() == roleId)
                .findFirst()
                .orElseThrow(() -> new RoleNotFoundException(roleId));

        if (role.getRoleType() == RoleType.OWNER || role.getRoleType() == RoleType.BASE) {
            throw new InvalidRoleDeletion("Base Roles: 'OWNER' and 'BASE' cannot be deleted");
        }

        boolean inUse = project.getProjectMembers().stream()
                .anyMatch(m -> m.getProjectRole().getId() == roleId);
        if (inUse) throw new InvalidRoleDeletion("Remove Role from ProjectMember before deleting the Role");
    }

    public static void validateRoleAssignment(Project project, int roleId, Long projectMemberId, String actingUserId) {
        ProjectRole newRole = project.getProjectRoles().stream()
                .filter(r -> r.getId() == roleId)
                .findFirst()
                .orElseThrow(() -> new RoleNotFoundException(roleId));

        ProjectMember targetMember = project.getProjectMembers().stream()
                .filter(m -> m.getId().equals(projectMemberId))
                .findFirst()
                .orElseThrow(() -> new ProjectMemberNotFoundException(projectMemberId));

        ProjectMember actingUser = project.getProjectMembers().stream()
                .filter(m -> m.getUser().getId().equals(actingUserId))
                .findFirst()
                .orElseThrow(() -> new UserNotProjectMemberException(actingUserId));

        ProjectRole currentRole = targetMember.getProjectRole();

        if (currentRole.getRoleType() == RoleType.OWNER && newRole.getRoleType() != RoleType.OWNER) {
            long ownerCount = project.getProjectMembers().stream()
                    .filter(m -> m.getProjectRole().getRoleType() == RoleType.OWNER)
                    .count();
            if (ownerCount <= 1) throw new InvalidRoleAssignmentException("At least one OWNER must exist in project");
        }

        if (newRole.getRoleType() == RoleType.OWNER &&
                targetMember.getUser().getId().equals(actingUserId) &&
                actingUser.getProjectRole().getRoleType() != RoleType.OWNER) {
            throw new InvalidRoleAssignmentException("You cannot assign yourself to OWNER role");
        }
    }
}