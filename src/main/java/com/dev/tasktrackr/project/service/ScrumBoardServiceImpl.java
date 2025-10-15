package com.dev.tasktrackr.project.service;

import com.dev.tasktrackr.project.api.dtos.mapper.ScrumBoardMapper;
import com.dev.tasktrackr.project.api.dtos.request.CreateCommentRequest;
import com.dev.tasktrackr.project.api.dtos.response.ScrumBoardResponseDto;
import com.dev.tasktrackr.project.api.dtos.response.SprintBacklogItemResponse;
import com.dev.tasktrackr.project.domain.Project;
import com.dev.tasktrackr.project.domain.scrum.ScrumDetails;
import com.dev.tasktrackr.project.domain.scrum.Sprint;
import com.dev.tasktrackr.project.domain.scrum.StoryStatus;
import com.dev.tasktrackr.project.repository.ProjectRepository;
import com.dev.tasktrackr.shared.exception.custom.NotFoundExceptions.ProjectNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ScrumBoardServiceImpl implements ScrumBoardService{
    private final ProjectRepository projectRepository;
    private final ScrumBoardMapper scrumBoardMapper;


    @Override
    public ScrumBoardResponseDto getScrumBoard(Long projectId, Long sprintId, String jwtUserId) {
        Project project = findProjectById(projectId);
        ScrumDetails scrumDetails = project.getScrumDetails();
        Sprint sprint = scrumDetails.findSprintById(sprintId); // check if active


        return scrumBoardMapper.toResponse(sprint, project.getProjectMembers());
    }

    @Override
    public SprintBacklogItemResponse updateUserStoryStatus(Long projectId, Long userStoryId, StoryStatus newStatus, String jwtUserId) {
        return null;
    }

    @Override
    public SprintBacklogItemResponse assignMemberToStory(Long projectId, Long userStoryId, Long memberId, String jwtUserId) {
        return null;
    }

    @Override
    public SprintBacklogItemResponse unassignMemberFromStory(Long projectId, Long userStoryId, Long memberId, String jwtUserId) {
        return null;
    }

    @Override
    public SprintBacklogItemResponse addCommentToStory(Long projectId, Long userStoryId, CreateCommentRequest commentRequest, String jwtUserId) {
        return null;
    }

    @Override
    public void removeCommentFromStory(Long projectId, Long userStoryId, Long commentId, String jwtUserId) {

    }

    @Override
    public SprintBacklogItemResponse addBlockerToStory(Long projectId, Long userStoryId, CreateCommentRequest commentRequest, String jwtUserId) {
        return null;
    }

    @Override
    public void removeBlockerFromStory(Long projectId, Long userStoryId, Long blockerId, String jwtUserId) {

    }

    private Project findProjectById(Long projectId) {
        return projectRepository.findById(projectId)
                .orElseThrow(() -> new ProjectNotFoundException(projectId));
    }
}
