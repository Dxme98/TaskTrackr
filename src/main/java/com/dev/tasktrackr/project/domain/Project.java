package com.dev.tasktrackr.project.domain;

import com.dev.tasktrackr.project.domain.ids.ProjectId;
import com.dev.tasktrackr.user.UserEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "projects")
@Getter
public class Project {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Getter(AccessLevel.NONE)
    private Long id;
    @Column(nullable = false, length = 255)
    private String name;
    @Column(name = "created_at", updatable = false, insertable = false)
    private LocalDateTime createdAt; // Default by Database
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "creator_id")
    private UserEntity creator;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_type_id")
    private ProjectType projectType;
    @OneToMany(mappedBy = "project", fetch = FetchType.LAZY, cascade = CascadeType.REMOVE)
    private Set<ProjectMember> projectMembers = new HashSet<>();
    @OneToMany(mappedBy = "project", fetch = FetchType.LAZY, cascade = CascadeType.REMOVE)
    private Set<ProjectInvite> projectInvites = new HashSet<>();


    public ProjectId getId() {
        return new ProjectId(id);
    }
}
