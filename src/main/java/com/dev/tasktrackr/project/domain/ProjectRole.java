package com.dev.tasktrackr.project.domain;

import com.dev.tasktrackr.project.domain.enums.PermissionName;
import com.dev.tasktrackr.project.domain.enums.ProjectType;
import com.dev.tasktrackr.project.domain.enums.RoleType;
import com.dev.tasktrackr.shared.exception.custom.InvalidRoleAssignmentException;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.*;

@Entity
@Getter
@NoArgsConstructor
@Table(name = "project_roles", uniqueConstraints = @UniqueConstraint(
        columnNames = {"name", "project_id"}))
public class ProjectRole {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(nullable = false, length = 36)
    private String name;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id", nullable = false)
    private Project project;

    // Enum direkt in der Join-Tabelle speichern
    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(
            name = "role_permissions",
            joinColumns = @JoinColumn(name = "role_id")
    )
    @Column(name = "permission_name", nullable = false)
    @Enumerated(EnumType.STRING)
    private Set<PermissionName> permissions = new HashSet<>();

    @Enumerated(EnumType.STRING)
    @Column(name = "role_type", nullable = false)
    private RoleType roleType;

    private ProjectRole(Project project, String name, RoleType roleType) {
        this.project = project;
        this.name = name;
        this.roleType = roleType;
    }

    public static ProjectRole createCustomRole(Project project, String name, Set<PermissionName> permissions) {
        validatePermissionsForProjectType(project.getProjectType(), permissions);

        ProjectRole role = new ProjectRole(project, name, RoleType.CUSTOM);
        role.permissions = permissions;

        return role;
    }

    public static ProjectRole createOwnerRole(Project project, ProjectType type) {
        ProjectRole role = new ProjectRole(project, "OWNER", RoleType.OWNER);

        if (type == ProjectType.BASIC) {
            // BASIC und COMMON Permissions hinzufügen
            role.permissions.addAll(getBasicPermissions());
            role.permissions.addAll(getCommonPermissions());
        }

        if (type == ProjectType.SCRUM) {
            // BASIC, COMMON und SCRUM Permissions hinzufügen
            role.permissions.addAll(getCommonPermissions());
          //  role.permissions.addAll(getScrumPermissions());
        }

        return role;
    }

    public static ProjectRole createBaseRole(Project project) {
        ProjectRole role = new ProjectRole(project, "BASE", RoleType.BASE);
        return role;
    }

    public boolean hasPermission(PermissionName permission) {
        return permissions.contains(permission);
    }

    public ProjectRole renameRole(String newName) {
        this.name = newName;
        return this;
    }

    // Helper Methods für Permission-Gruppen
    private static Set<PermissionName> getBasicPermissions() {
        return EnumSet.of(
                PermissionName.BASIC_CREATE_TASK,
                PermissionName.BASIC_DELETE_TASK,
                PermissionName.BASIC_EDIT_INFORMATION
        );
    }

    private static Set<PermissionName> getCommonPermissions() {
        return EnumSet.of(
                PermissionName.COMMON_INVITE_USER,
                PermissionName.COMMON_REMOVE_USER,
                PermissionName.COMMON_MANAGE_ROLES
        );
    }

    private static Set<PermissionName> getScrumPermissions() {
        // TODO

        return null;
    }


    private static void validatePermissionsForProjectType(ProjectType projectType, Set<PermissionName> permissions) {
        String allowedPrefix = (projectType == ProjectType.BASIC) ? "BASIC" : "SCRUM";

        Optional<PermissionName> invalidPermission = permissions.stream()
                .filter(p -> !(p.getName().startsWith("COMMON") || p.getName().startsWith(allowedPrefix) ))
                .findFirst();

        if (invalidPermission.isPresent()) {
            throw new InvalidRoleAssignmentException(invalidPermission.get(), projectType);
        }
    }

}
