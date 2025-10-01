package com.dev.tasktrackr.activity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.JdbcType;
import org.hibernate.dialect.PostgreSQLEnumJdbcType;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Instant;

@Entity
@Table(name = "project_activity")
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ProjectActivity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "project_id", nullable = false)
    private Long projectId;

    @Enumerated(EnumType.STRING)
    @Column(name = "activity_type", nullable = false)
    @JdbcType(PostgreSQLEnumJdbcType.class)
    private ActivityType activityType;

    @Column(name = "actor_id")
    private Long actorId;

    @Column(name = "actor_name", nullable = false, length = 50)
    private String actorName;

    @Column(name = "target_id")
    private Long targetId;

    @Column(name = "target_name", length = 50)
    private String targetName;

    @Enumerated(EnumType.STRING)
    @Column(name = "target_type")
    @JdbcType(PostgreSQLEnumJdbcType.class)
    private TargetType targetType;

    @Column(name = "context")
    private String context;

    @CreatedDate
    @Column(name = "created_at", updatable = false)
    private Instant createdAt;
}
