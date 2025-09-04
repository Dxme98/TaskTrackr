package com.dev.tasktrackr.project.domain;

import com.dev.tasktrackr.project.domain.enums.ProjectInviteStatus;
import com.dev.tasktrackr.shared.exception.custom.InviteIsNotPendingException;
import com.dev.tasktrackr.shared.exception.custom.UnauthorizedInviteHandleAcception;
import com.dev.tasktrackr.shared.exception.custom.UserAlreadyPartOfProjectException;
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
    @Column(name = "status")
    private ProjectInviteStatus inviteStatus;


    public static ProjectInvite createInvite(UserEntity sender, UserEntity receiver, Project project) {
        ProjectInvite projectInvite = new ProjectInvite();
        projectInvite.receiver = receiver;
        projectInvite.sender = sender;
        projectInvite.project = project;
        projectInvite.inviteStatus = ProjectInviteStatus.PENDING;

        return projectInvite;
    }

    public void decline(String jwtUserId) {
        validateResponse(jwtUserId);
        this.inviteStatus = ProjectInviteStatus.DECLINED;
    }

    public void accept(String jwtUserId) {
        validateResponse(jwtUserId);
        this.inviteStatus = ProjectInviteStatus.ACCEPTED;
    }


    /**
     * VALIDATION
     */
    public void validateResponse(String jwtUserId) {
        String receiverId = this.receiver.getId();

        // Prüfen ob jwtUserId = Empfänger
        if (!jwtUserId.equals(receiverId)) throw new UnauthorizedInviteHandleAcception(jwtUserId, receiverId);

        // Prüfen ob Status noch PENDING ist
        if (this.inviteStatus != ProjectInviteStatus.PENDING) throw new InviteIsNotPendingException(this.id);

        // Prüfen ob Empfänger nicht schon Mitglied des Projekts ist
        boolean receiverIsMember = this.project.getProjectMembers().stream()
                .anyMatch(member -> member.getUser().getId().equals(receiverId));
        if (receiverIsMember) throw new UserAlreadyPartOfProjectException(receiverId, this.project.getId());

    }

}
