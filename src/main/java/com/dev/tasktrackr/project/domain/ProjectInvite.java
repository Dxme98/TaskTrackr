package com.dev.tasktrackr.project.domain;

import com.dev.tasktrackr.project.domain.enums.ProjectInviteStatus;
import com.dev.tasktrackr.user.UserEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Instant;

@Entity
@Table(
        name = "project_invite",
        uniqueConstraints = @UniqueConstraint(
                columnNames = {"receiver_id", "sender_id", "project_id"}
        ),
        indexes = {
                @Index(name = "idx_project_invite_receiver_id", columnList = "receiver_id, invite_status_id"),
                @Index(name = "idx_project_invite_sender_id", columnList = "sender_id, invite_status_id")
        }
)
@Getter
@EntityListeners(AuditingEntityListener.class)
public class ProjectInvite {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;
    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "receiver_id", nullable = false)
    private UserEntity receiver;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sender_id") // optional, da ON DELETE SET NULL
    private UserEntity sender;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id", nullable = false)
    private Project project;

    @Enumerated(EnumType.STRING)
    private ProjectInviteStatus inviteStatus;


    public static ProjectInvite createInvite(UserEntity sender, UserEntity receiver, Project project) {
        ProjectInvite projectInvite = new ProjectInvite();
        projectInvite.receiver = receiver;
        projectInvite.sender = sender;
        projectInvite.project = project;
        projectInvite.inviteStatus = ProjectInviteStatus.PENDING;

        return projectInvite;
    }

}
