package com.dev.tasktrackr.project.domain;

import com.dev.tasktrackr.project.api.dtos.request.ProjectRequest;
import com.dev.tasktrackr.project.domain.basic.BasicDetails;
import com.dev.tasktrackr.project.domain.enums.PermissionName;
import com.dev.tasktrackr.project.domain.enums.ProjectType;
import com.dev.tasktrackr.project.domain.enums.RoleType;
import com.dev.tasktrackr.project.domain.scrum.ScrumDetails;
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

    public ProjectMember addMemberWithRole(UserEntity userEntity, ProjectRole role) {

        ProjectMember createdMember = ProjectMember.createMember(userEntity, this, role);
        projectMembers.add(createdMember);

        return createdMember;
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

    public ScrumDetails getScrumDetails() {
        if(this.projectType != ProjectType.SCRUM) throw new InvalidProjectTypeException("Project is not a SCRUM type");
        return scrumDetails;
    }

    public BasicDetails getBasicDetails() {
        if(this.projectType != ProjectType.BASIC) throw new InvalidProjectTypeException("Project is not a BASIC type");
        return basicDetails;
    }
}
