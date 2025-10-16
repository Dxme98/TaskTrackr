package com.dev.tasktrackr.project.domain;

import com.dev.tasktrackr.project.api.dtos.request.ProjectRequest;
import com.dev.tasktrackr.project.domain.basic.BasicDetails;
import com.dev.tasktrackr.project.domain.enums.PermissionName;
import com.dev.tasktrackr.project.domain.enums.ProjectType;
import com.dev.tasktrackr.project.domain.enums.RoleType;
import com.dev.tasktrackr.project.domain.scrum.ScrumDetails;
import com.dev.tasktrackr.project.domain.validator.ProjectValidator;
import com.dev.tasktrackr.shared.exception.custom.AccessDeniedExceptions.InvalidProjectTypeException;
import com.dev.tasktrackr.shared.exception.custom.AccessDeniedExceptions.UserNotProjectMemberException;
import com.dev.tasktrackr.shared.exception.custom.NotFoundExceptions.ProjectMemberNotFoundException;
import com.dev.tasktrackr.shared.exception.custom.NotFoundExceptions.RoleNotFoundException;
import com.dev.tasktrackr.user.UserEntity;
import jakarta.persistence.*;
import lombok.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Instant;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Entity
@Table(name = "projects")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
@Slf4j
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
    @OneToOne(mappedBy = "project", cascade = CascadeType.ALL, orphanRemoval = true)
    private ScrumDetails scrumDetails;
    @OneToOne(mappedBy = "project", cascade = CascadeType.ALL, orphanRemoval = true)
    private BasicDetails basicDetails;


    public static Project create(ProjectRequest projectRequest, UserEntity creator) {
        Project project = new Project();
        project.name = projectRequest.getName().trim();
        project.projectType = projectRequest.getProjectType();

        // Base-Rollen initialisieren
        project.initBaseRoles(project.projectType);

        // Details erstellen
        if (project.getProjectType() == ProjectType.BASIC) {
            project.assignBasicDetails();
        } else {
            project.assignScrumDetails();
        }

        // Creator automatisch als Owner hinzufügen
        project.addMemberWithRole(creator, project.getOwnerRole());


        return project;
    }

    public ProjectMember addMember(UserEntity userEntity) {
        ProjectValidator.validateAddMember(this, userEntity.getId());

        ProjectMember createdMember = ProjectMember.createMember(userEntity, this, getBaseRole());
        projectMembers.add(createdMember);

        return createdMember;
    }

    public ProjectMember addMemberWithRole(UserEntity userEntity, ProjectRole role) {
        ProjectValidator.validateAddMember(this, userEntity.getId());

        ProjectMember createdMember = ProjectMember.createMember(userEntity, this, role);
        projectMembers.add(createdMember);

        return createdMember;
    }

    public ProjectMember removeMember(Long memberToRemove) {
        ProjectValidator.validateRemoveMember(this, memberToRemove);
        ProjectMember toRemove = projectMembers.stream()
                .filter(member -> member.getId().equals(memberToRemove))
                .findFirst().orElseThrow(() -> new ProjectMemberNotFoundException(memberToRemove));

        projectMembers.remove(toRemove); // remove from project
        projectInvites.removeIf(invite -> invite.getReceiver().getId().equals(toRemove.getUser().getId())); // remove invite, to enable reinvite

        return toRemove;
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

    public ProjectRole deleteRole(int roleId) {
        ProjectValidator.validateRoleDeletion(this, roleId);

        Optional<ProjectRole> roleToDeleteOpt = this.projectRoles.stream()
                .filter(role -> role.getId() == roleId)
                .findFirst();

        if (roleToDeleteOpt.isPresent()) {
            ProjectRole roleToDelete = roleToDeleteOpt.get();
            this.projectRoles.remove(roleToDelete);
            return roleToDelete;
        }

        throw new RoleNotFoundException(roleId);
    }

    public ProjectMember assignRole(int roleId, Long projectMemberId, String actingUserId) {
        ProjectValidator.validateRoleAssignment(this, roleId, projectMemberId, actingUserId);

        ProjectRole role = this.projectRoles.stream()
                .filter(r -> r.getId() == roleId).findFirst()
                .orElseThrow(() -> new RoleNotFoundException(roleId));

        ProjectMember member = this.projectMembers.stream()
                .filter(m -> m.getId().equals(projectMemberId)).findFirst()
                .orElseThrow(() -> new ProjectMemberNotFoundException(projectMemberId));

        member.assignRole(role);

        return member;
    }

    private void assignScrumDetails() {
        if (this.projectType != ProjectType.SCRUM) {
            throw new InvalidProjectTypeException("Project is not a SCRUM type");
        }
        this.scrumDetails = new ScrumDetails(this);
    }

    private void assignBasicDetails() {
        if (this.projectType != ProjectType.BASIC) {
            throw new InvalidProjectTypeException("Project is not a BASIC type");
        }
      this.basicDetails = new BasicDetails(this);
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


    public ProjectMember findProjectMember(String userId) {
        return this.getProjectMembers().stream()
                .filter(member -> member.getUser().getId().equals(userId))
                .findFirst()
                .orElseThrow(() -> new UserNotProjectMemberException(userId));
    }

    public ProjectMember findProjectMember(Long memberId) {
        return this.getProjectMembers().stream()
                .filter(member -> member.getId().equals(memberId))
                .findFirst()
                .orElseThrow(() -> new ProjectMemberNotFoundException(memberId));
    }

    public void isProjectMember(String userId) {
        this.getProjectMembers().stream()
                .filter(member -> member.getUser().getId().equals(userId))
                .findFirst()
                .orElseThrow(() -> new UserNotProjectMemberException(userId));
    }

    public Set<ProjectMember> findProjectMembers(Set<Long> projectMemberIds) {
        Map<Long, ProjectMember> memberMap = this.projectMembers.stream()
                .collect(Collectors.toMap(ProjectMember::getId, Function.identity()));

        Set<Long> notFound = projectMemberIds.stream()
                .filter(id -> !memberMap.containsKey(id))
                .collect(Collectors.toSet());

        if (!notFound.isEmpty()) throw new ProjectMemberNotFoundException(notFound);

        return projectMemberIds.stream()
                .map(memberMap::get)
                .collect(Collectors.toSet());
    }

    public ScrumDetails getScrumDetails() {
        if(this.projectType != ProjectType.SCRUM) throw new InvalidProjectTypeException("Project is not a SCRUM type");
        return scrumDetails;
    }

    public BasicDetails getBasicDetails() {
        if(this.projectType != ProjectType.BASIC) throw new InvalidProjectTypeException("Project is not a BASIC type");
        return basicDetails;
    }
}
