package com.dev.tasktrackr.project.domain;

import com.dev.tasktrackr.project.domain.enums.PermissionName;
import com.dev.tasktrackr.project.domain.enums.RoleType;
import com.dev.tasktrackr.shared.exception.custom.AccessDeniedExceptions.PermissionDeniedException;
import com.dev.tasktrackr.shared.exception.custom.BadRequestExceptions.InvalidRoleAssignmentException;
import com.dev.tasktrackr.shared.exception.custom.ConflictExceptions.InvalidMemberRemovalException;
import com.dev.tasktrackr.user.UserEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Instant;

@Entity
@Table(
        name = "project_member",
        indexes = {
                @Index(name = "idx_project_member_project_id", columnList = "project_id")
        },
        uniqueConstraints = @UniqueConstraint(
                name = "uk_project_member_user_project",
                columnNames = {"user_id", "project_id"}
        )
)
@Getter
@NoArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class ProjectMember {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private UserEntity user;

    @ManyToOne
    @JoinColumn(name = "project_id", nullable = false)
    private Project project;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @JoinColumn(name = "role_id", nullable = false)
    @ManyToOne(fetch = FetchType.LAZY)
    private ProjectRole projectRole;


    // Intuitiver Konstruktor
    public ProjectMember(UserEntity user, Project project, ProjectRole projectRole) {
        this.user = user;
        this.project = project;
        this.projectRole = projectRole;
    }


    public static ProjectMember createMember(UserEntity user, Project project, ProjectRole projectRole) {
        return new ProjectMember(user, project, projectRole);
    }

    public void assignRole(ProjectRole newRole) {
        if (!newRole.getProject().equals(this.project)) {
            throw new InvalidRoleAssignmentException("Rolle gehört nicht zu diesem Projekt");
        }
        this.projectRole = newRole;
    }

    public void canBeRemovedFromProject() {
        if(getProjectRole().getRoleType().equals(RoleType.OWNER)) {
            throw new InvalidMemberRemovalException("Members with OWNER Role can't be removed.");
        }
    }

    // ====== Permission Checks ======

    public void canCreateTask() {
        if (!projectRole.hasPermission(PermissionName.BASIC_CREATE_TASK)) {
            throw new PermissionDeniedException("You do not have permission to create tasks.");
        }
    }

    public void canDeleteTask() {
        if (!projectRole.hasPermission(PermissionName.BASIC_DELETE_TASK)) {
            throw new PermissionDeniedException("You do not have permission to delete tasks.");
        }
    }

    public void canEditInformation() {
        if (!projectRole.hasPermission(PermissionName.BASIC_EDIT_INFORMATION)) {
            throw new PermissionDeniedException("You do not have permission to edit project information.");
        }
    }

    public void canInviteUser() {
        if (!projectRole.hasPermission(PermissionName.COMMON_INVITE_USER)) {
            throw new PermissionDeniedException("You do not have permission to invite users.");
        }
    }

    public void canRemoveUser() {
        if (!projectRole.hasPermission(PermissionName.COMMON_REMOVE_USER)) {
            throw new PermissionDeniedException("You do not have permission to remove users.");
        }
    }

    public void canManageRoles() {
        if (!projectRole.hasPermission(PermissionName.COMMON_MANAGE_ROLES)) {
            throw new PermissionDeniedException("You do not have permission to manage roles.");
        }
    }

    // SCRUM
    public void canCreateUserStory() {
        if (!projectRole.hasPermission(PermissionName.SCRUM_CREATE_USER_STORY)) {
            throw new PermissionDeniedException("You do not have permission to create user stories.");
        }
    }

    public void canDeleteUserStory() {
        if (!projectRole.hasPermission(PermissionName.SCRUM_DELETE_USER_STORY)) {
            throw new PermissionDeniedException("You do not have permission to delete user stories.");
        }
    }

    public void canPlanSprint() {
        if (!projectRole.hasPermission(PermissionName.SCRUM_PLAN_SPRINT)) {
            throw new PermissionDeniedException("You do not have permission to plan sprints.");
        }
    }

    public void canStartSprint() {
        if (!projectRole.hasPermission(PermissionName.SCRUM_START_SPRINT)) {
            throw new PermissionDeniedException("You do not have permission to start sprints.");
        }
    }

    public void canEndSprint() {
        if (!projectRole.hasPermission(PermissionName.SCRUM_END_SPRINT)) {
            throw new PermissionDeniedException("You do not have permission to end sprints.");
        }
    }

    public void canAssignUserToStory() {
        if (!projectRole.hasPermission(PermissionName.SCRUM_ASSIGN_USER_TO_STORY)) {
            throw new PermissionDeniedException("You do not have permission to assign users to stories.");
        }
    }

    public void canDeleteCommentsAndBlocker() {
        if (!projectRole.hasPermission(PermissionName.SCRUM_CAN_DELETE_COMMENTS_AND_BLOCKER)) {
            throw new PermissionDeniedException("You do not have permission to delete Comments.");
        }
    }

    public boolean canUpdateStoryStatus() {
        // Special handling to check extra permission (if assigned to task, user is also allowed to update even without permission)
        return projectRole.hasPermission(PermissionName.SCRUM_UPDATE_STORY_STATUS);
    }
}