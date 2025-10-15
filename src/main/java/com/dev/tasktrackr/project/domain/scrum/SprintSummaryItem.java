package com.dev.tasktrackr.project.domain.scrum;


import com.dev.tasktrackr.project.domain.Project;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;


@Entity
@Table(name = "sprint_summary_items", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"sprint_id", "user_story_id"})
})
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SprintSummaryItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "project_id", nullable = false)
    private Project project;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "sprint_id", nullable = false)
    private Sprint sprint;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_story_id", nullable = false)
    private UserStory userStory;

    @Column(nullable = false)
    private String title;

    @Column(name = "story_points", nullable = false)
    private Integer storyPoints;

    @Column(name = "is_completed", nullable = false)
    private boolean isCompleted;


    public static SprintSummaryItem create(UserStory userStory, Sprint sprint) {
        return SprintSummaryItem.builder()
                .project(sprint.getScrumDetails().getProject())
                .sprint(sprint)
                .userStory(userStory)
                .title(userStory.getTitle())
                .storyPoints(userStory.getStoryPoints())
                .isCompleted(false)
                .build();
    }

    public void complete() {
        this.isCompleted = true;
    }
    public void notComplete() {
        this.isCompleted = false;
    }
}
