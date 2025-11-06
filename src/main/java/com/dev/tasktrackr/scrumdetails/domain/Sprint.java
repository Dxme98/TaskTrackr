package com.dev.tasktrackr.scrumdetails.domain;

import com.dev.tasktrackr.scrumdetails.api.dtos.request.CreateCommentRequest;
import com.dev.tasktrackr.scrumdetails.api.dtos.request.CreateSprintRequest;
import com.dev.tasktrackr.project.domain.ProjectMember;
import com.dev.tasktrackr.shared.exception.custom.ConflictExceptions.SprintNotActiveException;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcType;
import org.hibernate.dialect.PostgreSQLEnumJdbcType;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
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

    public boolean isActive() {
        return this.status.equals(SprintStatus.ACTIVE);
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

    public SprintBacklogItem updateBacklogItemStatus(SprintBacklogItem sprintBacklogItem, StoryStatus newStatus, SprintSummaryItem sprintSummaryItem) {
        sprintIsActive();
        sprintBacklogItem.getUserStory().updateStatus(newStatus);

        if(newStatus == StoryStatus.DONE) {
            sprintSummaryItem.complete();
        } else {
            sprintSummaryItem.notComplete();
        }

        return sprintBacklogItem;
    }


    public SprintBacklogItem assignMemberToStory(SprintBacklogItem backlogItem, ProjectMember member) {
        sprintIsActive();
        return backlogItem.assignMember(member);
    }

    public SprintBacklogItem unassignMemberFromStory(SprintBacklogItem backlogItem, ProjectMember member) {
        sprintIsActive();
        return backlogItem.unassignMember(member);
    }

    public Comment addCommentToStory(SprintBacklogItem backlogItem,  ProjectMember member, CreateCommentRequest commentRequest) {
        sprintIsActive();
        return backlogItem.addComment(member, commentRequest);
    }

    public Comment addBlockerToStory(SprintBacklogItem backlogItem, ProjectMember member, CreateCommentRequest commentRequest) {
        sprintIsActive();
        return backlogItem.addBlocker(member, commentRequest);
    }

    public Comment removeCommentFromStory(SprintBacklogItem backlogItem, Comment comment) {
        sprintIsActive();
        return backlogItem.removeComment(comment);
    }

    void sprintIsActive() {
        if(!isActive()) throw new SprintNotActiveException(this.id);
    }
}
