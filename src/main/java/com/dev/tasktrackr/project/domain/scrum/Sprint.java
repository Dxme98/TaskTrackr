package com.dev.tasktrackr.project.domain.scrum;

import com.dev.tasktrackr.project.api.dtos.request.CreateSprintRequest;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcType;
import org.hibernate.dialect.PostgreSQLEnumJdbcType;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

@Entity
@Table(name = "sprints")
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Sprint {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "project_id", nullable = false)
    private ScrumDetails scrumDetails;

    @Column(nullable = false)
    private String name;

    @Lob
    private String description;

    @Column(name = "goal", nullable = false)
    private String goal;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @JdbcType(PostgreSQLEnumJdbcType.class)
    private SprintStatus status = SprintStatus.PLANNED;

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "end_date", nullable = false)
    private LocalDate endDate;

    @OneToMany(mappedBy = "sprint", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private Set<SprintBacklogItem> backlogItems = new HashSet<>();


    public static Sprint create(CreateSprintRequest createSprintRequest, ScrumDetails scrumDetails) {
        return Sprint.builder()
                .backlogItems(new HashSet<>())
                .startDate(createSprintRequest.getStartDate())
                .endDate(createSprintRequest.getEndDate())
                .description(createSprintRequest.getDescription())
                .name(createSprintRequest.getName())
                .status(SprintStatus.PLANNED)
                .goal(createSprintRequest.getGoal())
                .scrumDetails(scrumDetails)
                .build();
    }

    public Set<SprintBacklogItem> addUserStoriesToSprint(List<UserStory> userStories) {
        userStories
                .forEach(story -> backlogItems.add(SprintBacklogItem.create(story, this)));

        return backlogItems;
    }

    public Sprint start() {

        if (!this.status.equals(SprintStatus.PLANNED)) {
            throw new IllegalStateException("Only sprints with status PLANNED can be started.");
        }

        this.status = SprintStatus.ACTIVE;
        return this;
    }

    public Sprint end() {

        // TODO: Was passiert mit unfinished todos?

        if (!this.status.equals(SprintStatus.ACTIVE)) {
            throw new IllegalStateException("Only sprints with status ACTIVE can be finished.");
        }

        this.status = SprintStatus.DONE;
        return this;
    }

    private void updateBacklogItems(List<UserStory> userStories) {
        Set<SprintBacklogItem> newBacklogItems = new HashSet<>();

        for (UserStory story : userStories) {
            SprintBacklogItem newItem = SprintBacklogItem.create(story, this);
            newBacklogItems.add(newItem);
            story.updateStatus(StoryStatus.SPRINT_BACKLOG);
        }
        this.backlogItems = newBacklogItems;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Sprint sprint = (Sprint) o;
        return Objects.equals(scrumDetails, sprint.scrumDetails) &&
                Objects.equals(name, sprint.name) &&
                Objects.equals(description, sprint.description) &&
                Objects.equals(goal, sprint.goal) &&
                status == sprint.status &&
                Objects.equals(startDate, sprint.startDate) &&
                Objects.equals(endDate, sprint.endDate);
    }

    @Override
    public int hashCode() {
        return Objects.hash(scrumDetails, name, description, goal, status, startDate, endDate);
    }
}
