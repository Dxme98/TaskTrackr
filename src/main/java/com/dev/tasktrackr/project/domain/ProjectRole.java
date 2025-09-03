package com.dev.tasktrackr.project.domain;

import com.dev.tasktrackr.project.domain.enums.PermissionName;
import com.dev.tasktrackr.project.domain.enums.ProjectType;
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
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "role_permissions",
            joinColumns = @JoinColumn(name = "role_id"),
            inverseJoinColumns = @JoinColumn(name = "permission_id")
    )
    private List<Permission> permissions = new ArrayList<>();

    private ProjectRole(Project project, String name) {
        this.project = project;
        this.name = name;
    }

    public static ProjectRole createOwnerRole(Project project, ProjectType type) {
        ProjectRole role = new ProjectRole(project, "OWNER");

        List<Permission> allPermissions = initializeAllPermissions();

        if (type == ProjectType.BASIC) {
            List<Permission> ownerPermissions = allPermissions.stream()
                    .filter(p -> p.getType().equals("BASIC") || p.getType().equals("COMMON"))
                    .toList();
            role.permissions.addAll(ownerPermissions);
        }

        if(type == ProjectType.SCRUM) {}
        // SCRUM FEHLT

        return role;
    }

    public static ProjectRole createDefaultRole(Project project) {
        return new ProjectRole(project, "DEFAULT");
    }

    public boolean hasPermission(PermissionName permission) {
        return permissions.stream()
                .anyMatch(p -> p.getName() == permission);
    }

    public static List<Permission> initializeAllPermissions() {
        return Arrays.stream(PermissionName.values())
                .map(name -> {
                    Permission p = new Permission();
                    p.setName(name);   // dein Enum PermissionName
                    p.setType(extractType(name)); // BASIC, COMMON, SCRUM
                    return p;
                })
                .toList();
    }

    private static String extractType(PermissionName name) {
        if (name.name().startsWith("BASIC")) return "BASIC";
        if (name.name().startsWith("COMMON")) return "COMMON";
        if (name.name().startsWith("SCRUM")) return "SCRUM";
        throw new IllegalArgumentException("Unbekannter PermissionName: " + name);
    }
}
