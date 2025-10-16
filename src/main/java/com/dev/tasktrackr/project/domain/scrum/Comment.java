package com.dev.tasktrackr.project.domain.scrum;

import com.dev.tasktrackr.project.api.dtos.request.CreateCommentRequest;
import com.dev.tasktrackr.project.domain.ProjectMember;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Instant;

@Entity
@Table(name = "comments")
@Getter
@EntityListeners(AuditingEntityListener.class)
@Builder
public class Comment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "sprint_backlog_item_id", nullable = false)
    private SprintBacklogItem sprintBacklogItem;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by")
    private ProjectMember createdBy;

    @Column(length = 500, nullable = false)
    private String message;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CommentType type;

    @Column(name = "created_at")
    @CreatedDate
    private Instant createdAt;

    public static Comment createComment(ProjectMember member, CreateCommentRequest commentRequest, SprintBacklogItem sprintBacklogItem) {
        return  Comment.builder()
                .sprintBacklogItem(sprintBacklogItem)
                .message(commentRequest.getMessage())
                .createdBy(member)
                .type(CommentType.COMMENT)
                .build();
    }

    public static Comment createBlocker(ProjectMember member, CreateCommentRequest commentRequest, SprintBacklogItem sprintBacklogItem) {
        return  Comment.builder()
                .sprintBacklogItem(sprintBacklogItem)
                .message(commentRequest.getMessage())
                .createdBy(member)
                .type(CommentType.BLOCKER)
                .build();
    }
}
























