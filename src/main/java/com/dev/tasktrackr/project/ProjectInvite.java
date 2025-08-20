package com.dev.tasktrackr.project;

import com.dev.tasktrackr.user.UserEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

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
@Setter
public class ProjectInvite {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "created_at", insertable = false, updatable = false)
    private LocalDateTime createdAt;
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "receiver_id", nullable = false)
    private UserEntity receiver;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sender_id") // optional, da ON DELETE SET NULL
    private UserEntity sender;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id", nullable = false)
    private Project project;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "invite_status_id", nullable = false)
    private ProjectInviteStatus projectInviteStatus;

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
