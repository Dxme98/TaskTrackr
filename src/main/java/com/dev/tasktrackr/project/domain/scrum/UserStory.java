package com.dev.tasktrackr.project.domain.scrum;

import com.dev.tasktrackr.project.domain.enums.Priority;
import jakarta.persistence.*;
import lombok.Getter;
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
    private Priority priority;

    @Column(name = "story_points")
    private Integer storyPoints;

    @Column(nullable = false)
    @CreatedDate
    private Instant createdAt;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StoryStatus status;

    @OneToMany(mappedBy = "userStory", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Comment> comments; // this should be in SprintBacklogItem
}
