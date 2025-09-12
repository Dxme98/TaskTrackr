package com.dev.tasktrackr.project.domain;

import com.dev.tasktrackr.project.domain.enums.PermissionName;
import com.dev.tasktrackr.shared.exception.custom.AccessDeniedExceptions.PermissionDeniedException;
import com.dev.tasktrackr.shared.exception.custom.BadRequestExceptions.InvalidRoleAssignmentException;
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
}