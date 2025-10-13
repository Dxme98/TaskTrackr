package com.dev.tasktrackr.project.domain.scrum;

import com.dev.tasktrackr.project.api.dtos.request.CreateUserStoryRequest;
import com.dev.tasktrackr.project.domain.enums.Priority;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcType;
import org.hibernate.dialect.PostgreSQLEnumJdbcType;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Instant;
import java.util.Set;

@Entity
@Table(name = "user_stories", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"project_id", "title"})
})
@Getter
@EntityListeners(AuditingEntityListener.class)
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserStory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "project_id", nullable = false)
    private ScrumDetails scrumDetails;

    @Column(nullable = false)
    private String title;

    @Lob
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @JdbcType(PostgreSQLEnumJdbcType.class)
    private Priority priority;

    @Column(name = "story_points", nullable = false)
    private Integer storyPoints;

    @Column(nullable = false, name = "createdat")
    @CreatedDate
    private Instant createdAt;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @JdbcType(PostgreSQLEnumJdbcType.class)
    private StoryStatus status;


    public static UserStory create(CreateUserStoryRequest createUserStoryRequest, ScrumDetails scrumDetails) {
        return UserStory.builder()
                .priority(createUserStoryRequest.getPriority())
                .status(StoryStatus.NOT_ASSIGNED_TO_SPRINT)
                .title(createUserStoryRequest.getTitle())
                .description(createUserStoryRequest.getDescription())
                .storyPoints(createUserStoryRequest.getStoryPoints())
                .scrumDetails(scrumDetails)
                .build();
    }
}
