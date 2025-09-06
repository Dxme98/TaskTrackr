package com.dev.tasktrackr.project.domain;

import com.dev.tasktrackr.project.api.dtos.request.ProjectRequest;
import com.dev.tasktrackr.project.domain.enums.PermissionName;
import com.dev.tasktrackr.project.domain.enums.ProjectType;
import com.dev.tasktrackr.project.domain.enums.RoleType;
import com.dev.tasktrackr.project.domain.validator.ProjectValidator;
import com.dev.tasktrackr.shared.exception.custom.*;
import com.dev.tasktrackr.user.UserEntity;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@Table(name = "projects")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class Project {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false, length = 255)
    private String name;
    @CreatedDate
    @Column(name = "created_at", updatable = false, nullable = false)
    private Instant createdAt;
    @LastModifiedDate
    private Instant updatedAt;
    @Enumerated(EnumType.STRING)
    @Column(name = "type")
    private ProjectType projectType;
    @OneToMany(mappedBy = "project", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<ProjectMember> projectMembers = new HashSet<>();
    @OneToMany(mappedBy = "project", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ProjectInvite> projectInvites = new ArrayList<>();
    @OneToMany(mappedBy = "project", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ProjectRole> projectRoles = new ArrayList<>();


    public static Project create(ProjectRequest projectRequest, UserEntity creator) {
        Project project = new Project();
        project.name = projectRequest.getName().trim();
        project.projectType = projectRequest.getProjectType();

        // Base-Rollen initialisieren
        project.initBaseRoles(project.projectType);

        // Creator automatisch als Owner hinzufügen
        project.addMemberWithRole(creator, project.getOwnerRole());


        return project;
    }

    public void addMember(UserEntity userEntity) {
        ProjectValidator.validateAddMember(this, userEntity.getId());

        ProjectMember createdMember = ProjectMember.createMember(userEntity, this, getBaseRole());
        projectMembers.add(createdMember);
    }

    private void addMemberWithRole(UserEntity userEntity, ProjectRole role) {
        ProjectValidator.validateAddMember(this, userEntity.getId());

        ProjectMember createdMember = ProjectMember.createMember(userEntity, this, role);
        projectMembers.add(createdMember);
    }

    public void createInvite(UserEntity sender, UserEntity receiver) {
        ProjectValidator.validateInviteCreation(this, receiver.getId(), sender.getId());

        ProjectInvite createdInvite = ProjectInvite.createInvite(sender, receiver, this);
        projectInvites.add(createdInvite);
    }

    public void createRole(String name, Set<PermissionName> permissions) {
        ProjectValidator.validateRoleCreation(this, name);

        ProjectRole role  = ProjectRole.createCustomRole(this, name, permissions);
        projectRoles.add(role);
    }

    public void deleteRole(int roleId) {
        ProjectValidator.validateRoleDeletion(this, roleId);


        this.projectRoles.removeIf(role -> role.getId() == roleId);
    }

    public ProjectMember assignRole(int roleId, Long projectMemberId, String actingUserId) {
        ProjectValidator.validateRoleAssignment(this, roleId, projectMemberId, actingUserId);

        ProjectRole role = this.projectRoles.stream()
                .filter(r -> r.getId() == roleId).findFirst()
                .orElseThrow(() -> new RoleNotFoundException(roleId));

        ProjectMember member = this.projectMembers.stream()
                .filter(m -> m.getId() == projectMemberId).findFirst()
                .orElseThrow(() -> new ProjectMemberNotFoundException(projectMemberId));

        member.assignRole(role);

        return member;
    }

    public ProjectRole renameRole(int roleId, String newName) {
        ProjectValidator.validateRoleCreation(this, newName);
        ProjectRole role = this.projectRoles.stream()
                .filter(r -> r.getId() == roleId)
                .findFirst().orElseThrow(() -> new RoleNotFoundException(roleId));

        return role.renameRole(newName);
    }

    public void initBaseRoles(ProjectType projectType) {
        ProjectRole owner = ProjectRole.createOwnerRole(this, projectType);
        ProjectRole def = ProjectRole.createBaseRole(this);

        this.projectRoles.add(owner);
        this.projectRoles.add(def);
    }

    public ProjectRole getOwnerRole() {
        return projectRoles.stream()
                .filter(r -> r.getRoleType().equals(RoleType.OWNER))
                .findFirst()
                .orElseThrow(() -> new RoleNotFoundException("Owner role not initialized"));
    }

    public ProjectRole getBaseRole() {
        return projectRoles.stream()
                .filter(r -> r.getRoleType().equals(RoleType.BASE))
                .findFirst()
                .orElseThrow(() -> new RoleNotFoundException("Default role not initialized"));
    }
}
