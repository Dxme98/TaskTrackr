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
@Builder
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
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "creator_id")
    private UserEntity creator;
    @Enumerated(EnumType.STRING)
    @Column(name = "type")
    private ProjectType projectType;
    @OneToMany(mappedBy = "project", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<ProjectMember> projectMembers = new HashSet<>();
    @OneToMany(mappedBy = "project", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ProjectInvite> projectInvites = new ArrayList<>();

    public static Project create(ProjectRequest projectRequest, UserEntity creator) {
        Project project = new Project();
        project.name = projectRequest.getName().trim();
        project.creator = creator;
        project.projectType = projectRequest.getProjectType();

        return project;
    }

    public void addMember(UserEntity userEntity) {
        projectMembers.add(ProjectMember.createMember(userEntity, this));
    }

    public ProjectInvite createInvite(UserEntity sender, UserEntity receiver) {
        ProjectInvite createdInvite = ProjectInvite.createInvite(sender, receiver, this);
        projectInvites.add(createdInvite);

        return createdInvite;
    }

    /**
     * Methode wird verwendet, um den erstellten Invite aus dem Context zu laden -> keine extra Query!
     */
    public ProjectInvite findCreatedInvite() {
        return this.projectInvites.get(projectInvites.size()-1);
    }


}
