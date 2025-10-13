package com.dev.tasktrackr.project.domain.scrum;

import com.dev.tasktrackr.project.api.dtos.request.CreateSprintRequest;
import com.dev.tasktrackr.project.api.dtos.request.UpdateSprintRequest;
import jakarta.persistence.*;
import lombok.*;
import org.apache.catalina.User;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

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
    private SprintStatus status = SprintStatus.PLANNED;

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "end_date", nullable = false)
    private LocalDate endDate;

    @OneToMany(mappedBy = "sprint", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private Set<SprintBacklogItem> backlogItems = new HashSet<>();


    public static Sprint create(CreateSprintRequest createSprintRequest, ScrumDetails scrumDetails) {
        return Sprint.builder()
                .backlogItems(null)
                .startDate(createSprintRequest.getStartDate())
                .endDate(createSprintRequest.getEndDate())
                .description(createSprintRequest.getDescription())
                .name(createSprintRequest.getName())
                .status(createSprintRequest.getStatus())
                .goal(createSprintRequest.getGoal())
                .scrumDetails(scrumDetails)
                .build();
    }

    public Set<SprintBacklogItem> addUserStoriesToSprint(List<UserStory> userStories) {
        userStories
                .forEach(story -> backlogItems.add(SprintBacklogItem.create(story, this)));

        return backlogItems;
    }

    public void update(UpdateSprintRequest request, List<UserStory> userStories) {
        if (!this.status.equals(SprintStatus.PLANNED)) {
            throw new IllegalStateException("Only planned sprints can be edited.");
        }

        this.name = request.getName();
        this.description = request.getDescription();
        this.goal = request.getGoal();
        this.startDate = request.getStartDate();
        this.endDate = request.getEndDate();

        updateBacklogItems(userStories);
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
        // Bestehende User Story IDs extrahieren
        Set<Long> existingStoryIds = this.backlogItems.stream()
                .map(item -> item.getUserStory().getId())
                .collect(Collectors.toSet());

        // Neue User Story IDs
        Set<Long> newStoryIds = userStories.stream()
                .map(UserStory::getId)
                .collect(Collectors.toSet());

        // 1. Entferne Items, die nicht mehr in der neuen Liste sind
        this.backlogItems.removeIf(item -> !newStoryIds.contains(item.getUserStory().getId()));

        // 2. Füge neue User Stories hinzu, die noch nicht im Backlog sind
        userStories.stream()
                .filter(story -> !existingStoryIds.contains(story.getId()))
                .forEach(story -> this.backlogItems.add(SprintBacklogItem.create(story, this)));
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
