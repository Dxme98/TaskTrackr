package com.dev.tasktrackr.project.domain;

import com.dev.tasktrackr.user.UserEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Instant;

@Entity
@Table(
        name = "project_member",
        indexes = {
                @Index(name = "idx_project_member_project_id", columnList = "project_id")
        },
        uniqueConstraints = @UniqueConstraint(
                name = "uk_project_member_user_project",
                columnNames = {"user_id", "project_id"}
        )
)
@Getter
@NoArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class ProjectMember {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private UserEntity user;

    @ManyToOne
    @JoinColumn(name = "project_id", nullable = false)
    private Project project;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;


    // Intuitiver Konstruktor
    public ProjectMember(UserEntity user, Project project) {
        this.user = user;
        this.project = project;
    }


    public static ProjectMember createMember(UserEntity user, Project project) {
        return new ProjectMember(user, project);
    }


}