package com.dev.tasktrackr.project.domain;

import com.dev.tasktrackr.project.api.dtos.request.ProjectRequest;
import com.dev.tasktrackr.project.domain.enums.ProjectType;
import com.dev.tasktrackr.shared.exception.custom.ProjectInviteAlreadyExistsException;
import com.dev.tasktrackr.shared.exception.custom.UserAlreadyPartOfProjectException;
import com.dev.tasktrackr.shared.exception.custom.UserNotFoundException;
import com.dev.tasktrackr.shared.exception.custom.UserNotProjectMemberException;
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
    private Set<ProjectRole> projectRoles = new HashSet<>();

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

    public void initBaseRoles(ProjectType projectType) {
        ProjectRole owner = ProjectRole.createOwnerRole(this, projectType);
        ProjectRole def = ProjectRole.createDefaultRole(this);

        this.projectRoles.add(owner);
        this.projectRoles.add(def);
    }

    public void addMember(UserEntity userEntity) {
        ProjectMember createdMember = ProjectMember.createMember(userEntity, this, getDefaultRole());
        projectMembers.add(createdMember);
    }

    public void addMemberWithRole(UserEntity userEntity, ProjectRole role) {
        ProjectMember createdMember = ProjectMember.createMember(userEntity, this, role);
        projectMembers.add(createdMember);
    }

    public void createInvite(UserEntity sender, UserEntity receiver) {
        ProjectInvite createdInvite = ProjectInvite.createInvite(sender, receiver, this);
        projectInvites.add(createdInvite);
    }


    public ProjectInvite findCreatedInvite() {
        return this.projectInvites.get(projectInvites.size()-1);
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


}
