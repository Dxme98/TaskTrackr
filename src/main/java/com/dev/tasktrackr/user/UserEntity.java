package com.dev.tasktrackr.user;

import com.dev.tasktrackr.project.Project;
import com.dev.tasktrackr.project.ProjectInvite;
import com.dev.tasktrackr.project.ProjectMember;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@Table(
        name = "users",
        indexes = {
                @Index(name = "idx_users_username", columnList = "username")
        }
)
@Getter
@Setter
public class UserEntity {
    @Id
    @Column(length = 36)
    private String id;
    @Column(length = 32, nullable = false, unique = true)
    private String username;
    @Column(name = "created_at", updatable = false, insertable = false)
    private LocalDateTime createdAt; // Default by Database
    @OneToMany(mappedBy = "user", fetch = FetchType.LAZY,  cascade = CascadeType.REMOVE)
    private Set<ProjectMember> projectMemberships = new HashSet<>();
    @OneToMany(fetch = FetchType.LAZY, mappedBy = "receiver", cascade = CascadeType.REMOVE)
    private Set<ProjectInvite> receivedInvites = new HashSet<>();
    @OneToMany(fetch = FetchType.LAZY, mappedBy = "sender", cascade = CascadeType.REMOVE)
    private Set<ProjectInvite> sentInvites = new HashSet<>();
}
