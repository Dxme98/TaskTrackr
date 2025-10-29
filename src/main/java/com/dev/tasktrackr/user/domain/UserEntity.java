package com.dev.tasktrackr.user.domain;

import com.dev.tasktrackr.project.domain.ProjectInvite;
import com.dev.tasktrackr.project.domain.ProjectMember;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Instant;
import java.util.HashSet;
import java.util.Set;

@Entity
@EntityListeners(AuditingEntityListener.class)
@Table(
        name = "users",
        indexes = {
                @Index(name = "idx_users_username", columnList = "username")
        }
)
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserEntity {
    @Id
    @Column(length = 36)
    private String id;
    @Column(length = 32, nullable = false, unique = true)
    private String username;
    @CreatedDate
    @Column(name = "created_at", updatable = false, nullable = false)
    private Instant createdAt;
    @OneToMany(mappedBy = "user", fetch = FetchType.LAZY,  cascade = CascadeType.REMOVE)
    private Set<ProjectMember> projectMemberships = new HashSet<>();
    @OneToMany(fetch = FetchType.LAZY, mappedBy = "receiver", cascade = CascadeType.REMOVE)
    private Set<ProjectInvite> receivedInvites = new HashSet<>();
    @OneToMany(fetch = FetchType.LAZY, mappedBy = "sender", cascade = CascadeType.REMOVE)
    private Set<ProjectInvite> sentInvites = new HashSet<>();
}
