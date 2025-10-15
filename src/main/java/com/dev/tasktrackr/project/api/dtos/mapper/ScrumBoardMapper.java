package com.dev.tasktrackr.project.api.dtos.mapper;

import com.dev.tasktrackr.project.api.dtos.response.ScrumBoardResponseDto;
import com.dev.tasktrackr.project.api.dtos.response.SprintBacklogItemResponse;
import com.dev.tasktrackr.project.domain.ProjectMember;
import com.dev.tasktrackr.project.domain.scrum.Sprint;
import com.dev.tasktrackr.project.domain.scrum.StoryStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;


import java.util.Set;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class ScrumBoardMapper {
    private final SprintBacklogItemMapper sprintBacklogItemMapper;
    private final ProjectMemberMapper projectMemberMapper;

    public ScrumBoardResponseDto toResponse(Sprint sprint, Set<ProjectMember> projectMembers) {
        ScrumBoardResponseDto response = new ScrumBoardResponseDto();

        // 1. Basis-Attribute des Sprints mappen
        response.setSprintName(sprint.getName());
        response.setSprintDescription(sprint.getDescription());
        response.setSprintGoal(sprint.getGoal());
        response.setEndDate(sprint.getEndDate());
        response.setStartDate(sprint.getStartDate());

        // 2. Projektmitglieder mappen
        response.setProjectMembers(projectMembers.stream()
                    .map(projectMemberMapper::toResponse)
                    .collect(Collectors.toList()));


        // 3. Story Points berechnen
        int totalPoints = sprint.getBacklogItems().stream()
                .mapToInt(item -> item.getUserStory().getStoryPoints())
                .sum();
        int completedPoints = sprint.getBacklogItems().stream()
                .filter(item -> item.getUserStory().getStatus() == StoryStatus.DONE)
                .mapToInt(item -> item.getUserStory().getStoryPoints())
                .sum();
        response.setTotalStoryPoints(totalPoints);
        response.setCompletedStoryPoints(completedPoints);

        // 4. SprintBacklogItems in die korrekten Listen einfügen
        if (sprint.getBacklogItems() != null) {
            sprint.getBacklogItems().forEach(backlogItem -> {
                SprintBacklogItemResponse itemResponse = sprintBacklogItemMapper.toResponse(backlogItem);
                if (itemResponse != null && backlogItem.getUserStory() != null) {
                    switch (backlogItem.getUserStory().getStatus()) {
                        case SPRINT_BACKLOG:
                            response.getTodo().add(itemResponse);
                            break;
                        case IN_PROGRESS:
                            response.getInProgress().add(itemResponse);
                            break;
                        case REVIEW:
                            response.getReview().add(itemResponse);
                            break;
                        case DONE:
                            response.getDone().add(itemResponse);
                            break;
                    }
                }
            });
        }
        return response;
    }
}
