package com.dev.tasktrackr.project.domain.scrum;

import com.dev.tasktrackr.project.domain.ProjectMember;
import jakarta.persistence.*;
import lombok.Getter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Instant;

@Entity
@Table(name = "comments")
@Getter
@EntityListeners(AuditingEntityListener.class)
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
}
