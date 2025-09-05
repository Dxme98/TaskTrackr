package com.dev.tasktrackr.project.domain;

import com.dev.tasktrackr.project.api.dtos.request.ProjectRequest;
import com.dev.tasktrackr.project.domain.enums.PermissionName;
import com.dev.tasktrackr.project.domain.enums.ProjectType;
import com.dev.tasktrackr.shared.exception.custom.*;
import com.dev.tasktrackr.user.UserEntity;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Instant;
import java.time.LocalDateTime;
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

        // Default-Rollen initialisieren
        project.initBaseRoles(project.projectType);

        // Creator automatisch als Owner hinzufügen
        project.addMemberWithRole(creator, project.getOwnerRole());


        return project;
    }

    public void addMember(UserEntity userEntity) {
        ProjectMember createdMember = ProjectMember.createMember(userEntity, this, getDefaultRole());
        projectMembers.add(createdMember);
    }

    private void addMemberWithRole(UserEntity userEntity, ProjectRole role) {
        ProjectMember createdMember = ProjectMember.createMember(userEntity, this, role);
        projectMembers.add(createdMember);
    }

    public void createInvite(UserEntity sender, UserEntity receiver) {
        validateInviteCreation(receiver.getId(), sender.getId());
        ProjectInvite createdInvite = ProjectInvite.createInvite(sender, receiver, this);
        projectInvites.add(createdInvite);
    }

    public void createRole(String name, Set<PermissionName> permissions) {
        validateRoleCreation(name);
        ProjectRole role  = ProjectRole.createCustomRole(this, name, permissions);
        projectRoles.add(role);
    }

    public void deleteRole(int roleId) {
        validateRoleDeletion(roleId);
        this.projectRoles.removeIf(role -> role.getId() == roleId);
    }

    public ProjectMember assignRole(int roleId, Long projectMemberId) {
        validateRoleAssignment(projectMemberId);

        ProjectRole role = this.projectRoles.stream()
                .filter(r -> r.getId() == roleId).findFirst()
                .orElseThrow(() -> new RoleNotFoundException(roleId));

        ProjectMember member = this.projectMembers.stream()
                .filter(m -> m.getId() == projectMemberId).findFirst()
                .orElseThrow(() -> new ProjectMemberNotFoundException(projectMemberId));

        member.assignRole(role);

        return member;
    }






    public void initBaseRoles(ProjectType projectType) {
        ProjectRole owner = ProjectRole.createOwnerRole(this, projectType);
        ProjectRole def = ProjectRole.createDefaultRole(this);

        this.projectRoles.add(owner);
        this.projectRoles.add(def);
    }

    public ProjectRole getOwnerRole() {
        return projectRoles.stream()
                .filter(r -> r.getName().equalsIgnoreCase("OWNER"))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Owner role not initialized"));
    }

    public ProjectRole getDefaultRole() {
        return projectRoles.stream()
                .filter(r -> r.getName().equalsIgnoreCase("DEFAULT"))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Default role not initialized"));
    }





    /**
     * VALIDATION
     */
    public void validateInviteCreation(String receiverId, String senderId) {
        // Prüfen ob Einladung bereits existiert
        boolean inviteExists = this.projectInvites.stream()
                .anyMatch(invite -> invite.getReceiver().getId().equals(receiverId));
        if (inviteExists) throw new ProjectInviteAlreadyExistsException(receiverId, this.id);


        // Prüfen ob User bereits Teil des Projekts ist
        boolean receiverIsMember = this.projectMembers.stream()
                .anyMatch(member -> member.getUser().getId().equals(receiverId));
        if (receiverIsMember) throw new UserAlreadyPartOfProjectException(receiverId, this.id);


        // Prüfen ob Sender Mitglied des Projekts ist
        boolean senderIsMember = this.projectMembers.stream()
                .anyMatch(member -> member.getUser().getId().equals(senderId));
        if (!senderIsMember) throw new UserNotProjectMemberException(senderId);
    }

    public void validateRoleCreation(String roleName) {

        boolean nameExists = this.projectRoles.stream()
                .anyMatch(r -> r.getName().equalsIgnoreCase(roleName));

        if(nameExists) throw new RoleNameAlreadyExistsException(roleName);
    }

    public void validateRoleDeletion(int roleId) {
        ProjectRole role = this.projectRoles.stream()
                .filter(r -> r.getId() == roleId)
                .findFirst()
                .orElseThrow(() -> new RoleNotFoundException(roleId));

        // Default oder Owner dürfen nie gelöscht werden
        if (role.getName().equalsIgnoreCase("DEFAULT") || role.getName().equalsIgnoreCase("OWNER")) {
            throw new InvalidRoleDeletion("Base Roles: 'DEFAULT' and 'OWNER' can not be deleted");
        }

        // Prüfen ob User diese Rolle noch nutzen
        boolean usersWithRoleExists = this.projectMembers.stream()
                .anyMatch(member -> member.getProjectRole().getId() == roleId);

        if (usersWithRoleExists) {
            throw new InvalidRoleDeletion("Remove Role from Projectmember before deleting the Role");
        }
    }

    public void validateRoleAssignment(Long projectMemberId) {
        ProjectMember member = this.projectMembers.stream()
                .filter(m -> m.getId().equals(projectMemberId))
                .findFirst()
                .orElseThrow(() -> new ProjectMemberNotFoundException(projectMemberId));

        ProjectRole currentRole = member.getProjectRole();

        // Schutz: letzter Owner darf nicht degradiert werden
        if (currentRole.getName().equalsIgnoreCase("OWNER")) {

            long ownerCount = this.projectMembers.stream()
                    .filter(m -> m.getProjectRole().getName().equalsIgnoreCase("OWNER"))
                    .count();

            if (ownerCount <= 1) {
                throw new InvalidRoleAssignmentException("At least one OWNER must exist in project");
            }
        }
    }


}
