package com.dev.tasktrackr.project.domain.scrum;

import com.dev.tasktrackr.project.api.dtos.request.CreateCommentRequest;
import com.dev.tasktrackr.project.api.dtos.request.CreateSprintRequest;
import com.dev.tasktrackr.project.api.dtos.response.ActiveSprintData;
import com.dev.tasktrackr.project.domain.ProjectMember;
import com.dev.tasktrackr.shared.exception.custom.NotFoundExceptions.SprintBacklogItemNotFoundException;
import com.dev.tasktrackr.shared.exception.custom.NotFoundExceptions.SprintSummaryItemNotFoundException;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcType;
import org.hibernate.dialect.PostgreSQLEnumJdbcType;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
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

    @OneToMany(mappedBy = "sprint", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private Set<SprintSummaryItem> sprintSummaryItems = new HashSet<>();


    public static Sprint create(CreateSprintRequest createSprintRequest, ScrumDetails scrumDetails) {
        return Sprint.builder()
                .backlogItems(new HashSet<>())
                .sprintSummaryItems(new HashSet<>())
                .startDate(createSprintRequest.getStartDate())
                .endDate(createSprintRequest.getEndDate())
                .description(createSprintRequest.getDescription())
                .name(createSprintRequest.getName())
                .status(SprintStatus.PLANNED)
                .goal(createSprintRequest.getGoal())
                .scrumDetails(scrumDetails)
                .build();
    }

    public void addUserStoriesToSprint(List<UserStory> userStories) {
        userStories
                .forEach(
                        story -> backlogItems.add(SprintBacklogItem.create(story, this)));
    }

    public void addSprintSummaryItems(List<UserStory> userStories) {
        userStories
                .forEach(
                        story -> sprintSummaryItems.add(SprintSummaryItem.create(story, this)));
    }

    public Sprint start() {

        if (!this.status.equals(SprintStatus.PLANNED)) {
            throw new IllegalStateException("Only sprints with status PLANNED can be started.");
        }

        this.status = SprintStatus.ACTIVE;
        return this;
    }

    public Sprint end() {
        if (!this.status.equals(SprintStatus.ACTIVE)) {
            throw new IllegalStateException("Only sprints with status ACTIVE can be finished.");
        }
        handleUncompletedBacklogItems();
        this.status = SprintStatus.DONE;
        return this;
    }

    public ActiveSprintData getData() {
        int totalStories = this.backlogItems.size();

        int finishedStories = (int) this.backlogItems.stream()
                .filter(SprintBacklogItem::isCompleted)
                .count();

        int totalPoints = this.backlogItems.stream()
                .mapToInt(item -> item.getUserStory().getStoryPoints())
                .sum();

        int finishedPoints = this.backlogItems.stream()
                .filter(SprintBacklogItem::isCompleted)
                .mapToInt(item -> item.getUserStory().getStoryPoints())
                .sum();

        LocalDate today = LocalDate.now();

        long daysLeftLong = ChronoUnit.DAYS.between(today, this.endDate);
        int daysLeft = (int) Math.max(0, daysLeftLong);

        int averageDailyVelocity = 0;

        if (today.isAfter(this.startDate) || today.isEqual(this.startDate)) {
            long daysPassedLong = ChronoUnit.DAYS.between(this.startDate, today) + 1;
            int daysPassed = (int) daysPassedLong;

            if (daysPassed > 0 && finishedPoints > 0) {
                averageDailyVelocity = finishedPoints / daysPassed;
            }
        }
        return ActiveSprintData.builder()
                .totalStories(totalStories)
                .finishedStories(finishedStories)
                .totalPoints(totalPoints)
                .finishedPoints(finishedPoints)
                .daysLeft(daysLeft)
                .averageDailyVelocity(averageDailyVelocity)
                .build();
    }

    private void handleUncompletedBacklogItems() {
        this.backlogItems.removeIf(backlogItem -> {
            if(!backlogItem.isCompleted()) {
                backlogItem.detachFromSprint();
                return true;
            }
            return false;
        });
    }

    public SprintBacklogItem updateBacklogItemStatus(Long backlogItemId, StoryStatus newStatus) {
        SprintBacklogItem itemToUpdate = findBacklogItemById(backlogItemId);
        itemToUpdate.getUserStory().updateStatus(newStatus);
        Long userStoryId = itemToUpdate.getUserStory().getId();

        if(newStatus == StoryStatus.DONE) {
            markSprintSummaryItemAsComplete(userStoryId);
        } else {
            markSprintSummaryItemAsNotComplete(userStoryId);
        }

        return itemToUpdate;
    }

    public void markSprintSummaryItemAsComplete(Long userStoryId) {
        SprintSummaryItem itemToUpdate = findSprintSummaryItemByUserStoryId(userStoryId);
        itemToUpdate.complete();
    }

    public void markSprintSummaryItemAsNotComplete(Long userStoryId) {
        SprintSummaryItem itemToUpdate = findSprintSummaryItemByUserStoryId(userStoryId);
        itemToUpdate.notComplete();
    }

    public SprintBacklogItem assignMemberToStory(Long backlogItemId, ProjectMember member) {
        SprintBacklogItem backlogItem = findBacklogItemById(backlogItemId);
        return backlogItem.assignMember(member);
    }

    public SprintBacklogItem unassignMemberFromStory(Long backlogItemId, ProjectMember member) {
        SprintBacklogItem backlogItem = findBacklogItemById(backlogItemId);
        return backlogItem.unassignMember(member);
    }

    public SprintBacklogItem addCommentToStory(Long backlogItemId, ProjectMember member, CreateCommentRequest commentRequest) {
        SprintBacklogItem backlogItem = findBacklogItemById(backlogItemId);
        return backlogItem.addComment(member, commentRequest);
    }

    public SprintBacklogItem addBlockerToStory(Long backlogItemId, ProjectMember member, CreateCommentRequest commentRequest) {
        SprintBacklogItem backlogItem = findBacklogItemById(backlogItemId);
        return backlogItem.addBlocker(member, commentRequest);
    }

    public Comment removeCommentFromStory(Long backlogItemId, Long commentId) {
        SprintBacklogItem backlogItem = findBacklogItemById(backlogItemId);
        return backlogItem.removeComment(commentId);
    }


    public SprintBacklogItem findBacklogItemById(Long backlogId) {
        return backlogItems.stream()
                .filter(item -> item.getId().equals(backlogId))
                .findFirst().orElseThrow(() -> new SprintBacklogItemNotFoundException(backlogId));
    }

    public SprintSummaryItem findSprintSummaryItemByUserStoryId(Long userStoryId) {
        return sprintSummaryItems.stream()
                .filter(item -> item.getUserStory().getId().equals(userStoryId))
                .findFirst().orElseThrow(() -> new SprintSummaryItemNotFoundException(userStoryId));
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
