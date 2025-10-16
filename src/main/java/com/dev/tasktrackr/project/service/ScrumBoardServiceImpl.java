package com.dev.tasktrackr.project.service;

import com.dev.tasktrackr.project.api.dtos.mapper.ScrumBoardMapper;
import com.dev.tasktrackr.project.api.dtos.mapper.SprintBacklogItemMapper;
import com.dev.tasktrackr.project.api.dtos.request.CreateCommentRequest;
import com.dev.tasktrackr.project.api.dtos.response.ScrumBoardResponseDto;
import com.dev.tasktrackr.project.api.dtos.response.SprintBacklogItemResponse;
import com.dev.tasktrackr.project.domain.Project;
import com.dev.tasktrackr.project.domain.ProjectMember;
import com.dev.tasktrackr.project.domain.scrum.*;
import com.dev.tasktrackr.project.repository.ProjectRepository;
import com.dev.tasktrackr.shared.exception.custom.NotFoundExceptions.ProjectNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ScrumBoardServiceImpl implements ScrumBoardService{
    private final ProjectRepository projectRepository;
    private final ScrumBoardMapper scrumBoardMapper;
    private final SprintBacklogItemMapper sprintBacklogItemMapper;

    // TODO: Berechtigungsprüfungen und Validierungen hinzufügen

    @Override
    public ScrumBoardResponseDto getScrumBoard(Long projectId,  String jwtUserId) {
        Project project = findProjectById(projectId);
        ScrumDetails scrumDetails = project.getScrumDetails();
        Sprint sprint = scrumDetails.findActiveSprint(); // check if active


        return scrumBoardMapper.toResponse(sprint, project.getProjectMembers());
    }

    @Override
    public SprintBacklogItemResponse updateUserStoryStatus(Long projectId, Long backlogItemId, StoryStatus newStatus, String jwtUserId) {
        Project project = findProjectById(projectId);
        ScrumDetails scrumDetails = project.getScrumDetails();

        SprintBacklogItem backlogItem = scrumDetails.updateBacklogItemStatusInActiveSprint( backlogItemId, newStatus);;
        projectRepository.save(project); // should update weil ist nicht neu? checken

        return sprintBacklogItemMapper.toResponse(backlogItem);
    }

    @Override
    public SprintBacklogItemResponse assignMemberToStory(Long projectId, Long backlogItemId, Long memberId, String jwtUserId) {
        Project project = findProjectById(projectId);
        ScrumDetails scrumDetails = project.getScrumDetails();
        ProjectMember member = project.findProjectMember(memberId);

        SprintBacklogItem backlogItem = scrumDetails.assignMemberToStory(backlogItemId, member);

        projectRepository.save(project);

        return sprintBacklogItemMapper.toResponse(backlogItem);
    }

    @Override
    public SprintBacklogItemResponse unassignMemberFromStory(Long projectId, Long backlogItemId, Long memberId, String jwtUserId) {
        Project project = findProjectById(projectId);
        ScrumDetails scrumDetails = project.getScrumDetails();
        ProjectMember member = project.findProjectMember(memberId);

        SprintBacklogItem backlogItem = scrumDetails.unassignMemberFromStory(backlogItemId, member);

        projectRepository.save(project);

        return sprintBacklogItemMapper.toResponse(backlogItem);
    }

    @Override
    public SprintBacklogItemResponse addCommentToStory(Long projectId, Long backlogItemId, CreateCommentRequest commentRequest, String jwtUserId) {
        Project project = findProjectById(projectId);
        ScrumDetails scrumDetails = project.getScrumDetails();
        ProjectMember member = project.findProjectMember(jwtUserId);

        SprintBacklogItem backlogItem = scrumDetails.addCommentToStory(backlogItemId, member, commentRequest);
        // ID COULD BE NULL

        return sprintBacklogItemMapper.toResponse(backlogItem);
    }

    @Override
    public void removeCommentFromStory(Long projectId, Long backlogItemId, Long commentId, String jwtUserId) {

    }

    @Override
    public SprintBacklogItemResponse addBlockerToStory(Long projectId, Long backlogItemId, CreateCommentRequest commentRequest, String jwtUserId) {
        Project project = findProjectById(projectId);
        ScrumDetails scrumDetails = project.getScrumDetails();
        ProjectMember member = project.findProjectMember(jwtUserId);

        SprintBacklogItem backlogItem = scrumDetails.addBlockerToStory(backlogItemId, member, commentRequest);

        // ID COULD BE NULL!

        return sprintBacklogItemMapper.toResponse(backlogItem);
    }

    @Override
    public void removeBlockerFromStory(Long projectId, Long backlogItemId, Long blockerId, String jwtUserId) {

    }

    private Project findProjectById(Long projectId) {
        return projectRepository.findById(projectId)
                .orElseThrow(() -> new ProjectNotFoundException(projectId));
    }
}
