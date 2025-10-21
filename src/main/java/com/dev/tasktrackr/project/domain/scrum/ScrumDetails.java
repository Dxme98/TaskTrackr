package com.dev.tasktrackr.project.domain.scrum;

import com.dev.tasktrackr.project.api.dtos.request.CreateCommentRequest;
import com.dev.tasktrackr.project.api.dtos.request.CreateSprintRequest;
import com.dev.tasktrackr.project.api.dtos.request.CreateUserStoryRequest;
import com.dev.tasktrackr.project.api.dtos.response.ActiveSprintData;
import com.dev.tasktrackr.project.api.dtos.response.ScrumMemberStatisticDto;
import com.dev.tasktrackr.project.api.dtos.response.ScrumProjectStatisticsDto;
import com.dev.tasktrackr.project.domain.Project;
import com.dev.tasktrackr.project.domain.ProjectMember;
import com.dev.tasktrackr.shared.exception.custom.ConflictExceptions.ActiveSprintAlreadyExistsException;
import com.dev.tasktrackr.shared.exception.custom.NotFoundExceptions.NoActiveSprintFoundException;
import com.dev.tasktrackr.shared.exception.custom.NotFoundExceptions.SprintNotFoundException;
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

    public SprintBacklogItem updateBacklogItemStatusInActiveSprint(Long backlogItemId, StoryStatus newStatus, ProjectMember member) {
        Sprint activeSprint = findActiveSprint();
        return activeSprint.updateBacklogItemStatus(backlogItemId, newStatus, member);
    }

    public SprintBacklogItem assignMemberToStory(Long backlogItemId, ProjectMember member) {
        Sprint activeSprint = findActiveSprint();
        return activeSprint.assignMemberToStory(backlogItemId, member);
    }

    public SprintBacklogItem unassignMemberFromStory(Long backlogItemId, ProjectMember member) {
        Sprint activeSprint = findActiveSprint();
        return activeSprint.unassignMemberFromStory(backlogItemId, member);
    }

    public SprintBacklogItem addCommentToStory(Long backlogItemId, ProjectMember member, CreateCommentRequest commentRequest) {
        Sprint activeSprint = findActiveSprint();
        return activeSprint.addCommentToStory(backlogItemId, member, commentRequest);
    }

    public SprintBacklogItem addBlockerToStory(Long backlogItemId, ProjectMember member, CreateCommentRequest commentRequest) {
        Sprint activeSprint = findActiveSprint();
        return activeSprint.addBlockerToStory(backlogItemId, member, commentRequest);
    }

    public Comment removeCommentFromStory(Long backlogItemId, Long commentId) {
        Sprint activeSprint = findActiveSprint();
        return activeSprint.removeCommentFromStory(backlogItemId, commentId);
    }

    public void startSprint(Sprint sprintToStart) {
        sprintToStart.start();
    }

    public Sprint endSprint(Long sprintId) {
        Sprint sprintToEnd = findSprintById(sprintId);
        return  sprintToEnd.end();
    }

    public ActiveSprintData getActiveSprintData() {
        try {
            Sprint activeSprint = findActiveSprint();
            return activeSprint.getData();
        } catch (NoActiveSprintFoundException ex) {
            return ActiveSprintData.builder().build();
        }
    }

    public Sprint findActiveSprint() {
        return sprints.stream()
                .filter(s -> s.getStatus().equals(SprintStatus.ACTIVE))
                .findFirst()
                .orElseThrow(() -> new NoActiveSprintFoundException(project.getId()));
    }
    private boolean hasActiveSprint() {
        return sprints.stream()
                .anyMatch(s -> s.getStatus().equals(SprintStatus.ACTIVE));
    }

    public Sprint findSprintById(Long sprintId) {
        return sprints.stream()
                .filter(s -> s.getId().equals(sprintId))
                .findFirst()
                .orElseThrow(() -> new SprintNotFoundException(sprintId));
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

