package com.dev.tasktrackr.project.domain.scrum;

import com.dev.tasktrackr.project.api.dtos.request.CreateCommentRequest;
import com.dev.tasktrackr.project.api.dtos.request.CreateSprintRequest;
import com.dev.tasktrackr.project.api.dtos.request.CreateUserStoryRequest;
import com.dev.tasktrackr.project.api.dtos.response.ActiveSprintData;
import com.dev.tasktrackr.project.api.dtos.response.ScrumMemberStatisticDto;
import com.dev.tasktrackr.project.api.dtos.response.ScrumProjectStatisticsDto;
import com.dev.tasktrackr.project.domain.Project;
import com.dev.tasktrackr.project.domain.ProjectMember;
import com.dev.tasktrackr.shared.exception.custom.ConflictExceptions.SprintNotActiveException;
import com.dev.tasktrackr.shared.exception.custom.NotFoundExceptions.NoActiveSprintFoundException;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.*;
import java.util.stream.Collectors;


@Entity
@Table(name = "scrum_details")
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class ScrumDetails {
    @Id
    private Long id;
    @OneToOne
    @MapsId
    @JoinColumn(name = "project_id")
    private Project project;

    @OneToMany(mappedBy = "scrumDetails", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<UserStory> userStories = new ArrayList<>();

    @OneToMany(mappedBy = "scrumDetails", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private Set<Sprint> sprints = new HashSet<>();

    public ScrumDetails(Project project) {
        this.project = project;
    }


    public UserStory createUserStory(CreateUserStoryRequest createUserStoryRequest) {
        return UserStory.create(createUserStoryRequest, this);
    }


    public Sprint createSprint(CreateSprintRequest createSprintRequest) {
        Sprint createdSprint = Sprint.create(createSprintRequest, this);
        sprints.add(createdSprint);

        return createdSprint;
    }

    public SprintBacklogItem updateBacklogItemStatus(SprintBacklogItem backlogItem, StoryStatus newStatus, Sprint sprint, SprintSummaryItem sprintSummaryItem) {

        if(!sprint.isActive()) throw new SprintNotActiveException(sprint.getId());

        return sprint.updateBacklogItemStatus(backlogItem, newStatus, sprintSummaryItem);
    }

    public SprintBacklogItem assignMemberToStory(SprintBacklogItem backlogItem, ProjectMember member, Sprint sprint) {

        if(!sprint.isActive()) throw new SprintNotActiveException(sprint.getId());

        return sprint.assignMemberToStory(backlogItem, member);
    }

    public SprintBacklogItem unassignMemberFromStory(SprintBacklogItem backlogItem, ProjectMember member, Sprint sprint) {

        if(!sprint.isActive()) throw new SprintNotActiveException(sprint.getId());

        return sprint.unassignMemberFromStory(backlogItem, member);
    }

    public Comment addCommentToStory(SprintBacklogItem backlogItem, ProjectMember member, CreateCommentRequest commentRequest, Sprint sprint) {

        if(!sprint.isActive()) throw new SprintNotActiveException(sprint.getId());

        return sprint.addCommentToStory(backlogItem, member, commentRequest);
    }

    public Comment addBlockerToStory(SprintBacklogItem backlogItem, ProjectMember member, CreateCommentRequest commentRequest, Sprint sprint) {

        if(!sprint.isActive()) throw new SprintNotActiveException(sprint.getId());

        return sprint.addBlockerToStory(backlogItem, member, commentRequest);
    }

    public Comment removeCommentFromStory(SprintBacklogItem backlogItem, Comment comment, Sprint sprint) {

        if(!sprint.isActive()) throw new SprintNotActiveException(sprint.getId());

       return sprint.removeCommentFromStory(backlogItem, comment);
    }

    public void startSprint(Sprint sprintToStart) {
        sprintToStart.start();
    }

    public Sprint endSprint(Sprint sprintToEnd) {
        return  sprintToEnd.end();
    }

    public Sprint findActiveSprint() {
        return sprints.stream()
                .filter(s -> s.getStatus().equals(SprintStatus.ACTIVE))
                .findFirst()
                .orElseThrow(() -> new NoActiveSprintFoundException(project.getId()));
    }

    public List<ScrumMemberStatisticDto> getMemberStatisticsList() {

        Sprint activeSprint;
        try {
            activeSprint = findActiveSprint();
        } catch (NoActiveSprintFoundException e) {
            return project.getProjectMembers().stream()
                    .map(member -> ScrumMemberStatisticDto.builder()
                            .username(member.getUser().getUsername())
                            .build())
                    .collect(Collectors.toList());
        }

        Set<SprintBacklogItem> sprintItems = activeSprint.getBacklogItems();

        Map<ProjectMember, List<SprintBacklogItem>> itemsByMember = new HashMap<>();
        project.getProjectMembers().forEach(member -> itemsByMember.put(member, new ArrayList<>()));

        for (SprintBacklogItem item : sprintItems) {
            for (ProjectMember assignedMember : item.getAssignedMembers()) {
                if (itemsByMember.containsKey(assignedMember)) {
                    itemsByMember.get(assignedMember).add(item);
                }
            }
        }

        // 6. Die Statistik-DTOs für jedes Mitglied erstellen
        return itemsByMember.entrySet().stream()
                .map(entry -> {
                    ProjectMember member = entry.getKey();
                    List<SprintBacklogItem> memberItems = entry.getValue();

                    int totalTasks = memberItems.size();
                    int finishedTasks = 0;
                    int totalPoints = 0;
                    int finishedPoints = 0;
                    int totalBlocker = 0;
                    int tasksInBacklog = 0;
                    int tasksInProgress = 0;
                    int tasksInReview = 0;
                    int tasksInDone = 0;

                    for (SprintBacklogItem item : memberItems) {
                        UserStory story = item.getUserStory();
                        totalPoints += story.getStoryPoints();

                        totalBlocker += (int) item.getComments().stream()
                                .filter(Comment::isBlocker)
                                .count();

                        switch (story.getStatus()) {
                            case SPRINT_BACKLOG:
                                tasksInBacklog++;
                                break;
                            case IN_PROGRESS:
                                tasksInProgress++;
                                break;
                            case REVIEW:
                                tasksInReview++;
                                break;
                            case DONE:
                                tasksInDone++;
                                finishedTasks++;
                                finishedPoints += story.getStoryPoints();
                                break;
                            default:
                                // Ignoriert andere Status wie NOT_ASSIGNED_TO_SPRINT
                                break;
                        }
                    }

                    int percentage = (totalTasks == 0) ? 0 : (int) Math.round(((double) finishedTasks / totalTasks) * 100);

                    return ScrumMemberStatisticDto.builder()
                            .username(member.getUser().getUsername()) // ANNNAHME 1
                            .totalTasks(totalTasks)
                            .finishedTasks(finishedTasks)
                            .finishedTasksPercentage(percentage)
                            .totalPoints(totalPoints)
                            .finishedPoints(finishedPoints)
                            .totalBlocker(totalBlocker)
                            .tasksInBacklog(tasksInBacklog)
                            .tasksInProgress(tasksInProgress)
                            .tasksInReview(tasksInReview)
                            .tasksInDone(tasksInDone)
                            .build();
                })
                .collect(Collectors.toList());
    }

    public ScrumProjectStatisticsDto getProjectStatistics() {
        List<Sprint> finishedSprints = this.sprints.stream()
                .filter(sprint -> sprint.getStatus().equals(SprintStatus.DONE))
                .toList();

        int finishedSprintsCount = finishedSprints.size();

        int totalCompletedPoints = finishedSprints.stream()
                .flatMap(sprint -> sprint.getBacklogItems().stream())
                .mapToInt(item -> item.getUserStory().getStoryPoints())
                .sum();


        int averageVelocity = 0;
        if (finishedSprintsCount > 0) {
            averageVelocity = totalCompletedPoints / finishedSprintsCount;
        }

        return ScrumProjectStatisticsDto.builder()
                .finishedSprints(finishedSprintsCount)
                .totalCompletedPoints(totalCompletedPoints)
                .averageVelocity(averageVelocity)
                .build();
    }
}

