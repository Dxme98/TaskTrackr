package com.dev.tasktrackr.project.service;

import com.dev.tasktrackr.activity.ProjectActivityEvents;
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
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ScrumBoardServiceImpl implements ScrumBoardService{
    private final ProjectRepository projectRepository;
    private final ScrumBoardMapper scrumBoardMapper;
    private final SprintBacklogItemMapper sprintBacklogItemMapper;
    private final ApplicationEventPublisher applicationEventPublisher;

    // TODO: Berechtigungsprüfungen und Validierungen hinzufügen

    @Override
    @Transactional(readOnly = true)
    public ScrumBoardResponseDto getScrumBoard(Long projectId,  String jwtUserId) {
        Project project = findProjectById(projectId);
        ScrumDetails scrumDetails = project.getScrumDetails();
        Sprint sprint = scrumDetails.findActiveSprint();


        return scrumBoardMapper.toResponse(sprint, project.getProjectMembers());
    }

    @Override
    @Transactional
    public SprintBacklogItemResponse updateUserStoryStatus(Long projectId, Long backlogItemId, StoryStatus newStatus, String jwtUserId) {
        Project project = findProjectById(projectId);
        ScrumDetails scrumDetails = project.getScrumDetails();
        ProjectMember member = project.findProjectMember(jwtUserId);

        SprintBacklogItem backlogItem = scrumDetails.updateBacklogItemStatusInActiveSprint( backlogItemId, newStatus, member);

        projectRepository.save(project);


        var event = new ProjectActivityEvents.UserStoryStatusUpdatedEvent(
                projectId, member.getId(), member.getUser().getUsername(),
                backlogItem.getUserStory().getId(), backlogItem.getUserStory().getTitle(), newStatus.name());
        applicationEventPublisher.publishEvent(event);

        return sprintBacklogItemMapper.toResponse(backlogItem);
    }

    @Override
    @Transactional
    public SprintBacklogItemResponse assignMemberToStory(Long projectId, Long backlogItemId, Long memberId, String jwtUserId) {
        Project project = findProjectById(projectId);
        ScrumDetails scrumDetails = project.getScrumDetails();
        ProjectMember member = project.findProjectMember(memberId);
        ProjectMember requestedMember = project.findProjectMember(jwtUserId);

        requestedMember.canAssignUserToStory();

        SprintBacklogItem backlogItem = scrumDetails.assignMemberToStory(backlogItemId, member);

        projectRepository.save(project);

        return sprintBacklogItemMapper.toResponse(backlogItem);
    }

    @Override
    @Transactional
    public SprintBacklogItemResponse unassignMemberFromStory(Long projectId, Long backlogItemId, Long memberId, String jwtUserId) {
        Project project = findProjectById(projectId);
        ScrumDetails scrumDetails = project.getScrumDetails();
        ProjectMember member = project.findProjectMember(memberId);
        ProjectMember requestedMember = project.findProjectMember(jwtUserId);

        requestedMember.canAssignUserToStory();

        SprintBacklogItem backlogItem = scrumDetails.unassignMemberFromStory(backlogItemId, member);

        projectRepository.save(project);

        return sprintBacklogItemMapper.toResponse(backlogItem);
    }

    @Override
    @Transactional
    public SprintBacklogItemResponse addCommentToStory(Long projectId, Long backlogItemId, CreateCommentRequest commentRequest, String jwtUserId) {
        Project project = findProjectById(projectId);
        ScrumDetails scrumDetails = project.getScrumDetails();
        ProjectMember member = project.findProjectMember(jwtUserId);

        SprintBacklogItem backlogItem = scrumDetails.addCommentToStory(backlogItemId, member, commentRequest);


        projectRepository.save(project);

        var event = new ProjectActivityEvents.CommentCreatedEvent(
                projectId, member.getId(), member.getUser().getUsername(),
                backlogItem.getUserStory().getId(), backlogItem.getUserStory().getTitle());
        applicationEventPublisher.publishEvent(event);

        return sprintBacklogItemMapper.toResponse(backlogItem);
    }

    @Override
    @Transactional
    public void removeCommentFromStory(Long projectId, Long backlogItemId, Long commentId, String jwtUserId) {
        Project project = findProjectById(projectId);
        ScrumDetails scrumDetails = project.getScrumDetails();
        ProjectMember member = project.findProjectMember(jwtUserId);

        member.canDeleteCommentsAndBlocker();

        scrumDetails.removeCommentFromStory(backlogItemId, commentId);

        projectRepository.save(project);
    }

    @Override
    @Transactional
    public SprintBacklogItemResponse addBlockerToStory(Long projectId, Long backlogItemId, CreateCommentRequest commentRequest, String jwtUserId) {
        Project project = findProjectById(projectId);
        ScrumDetails scrumDetails = project.getScrumDetails();
        ProjectMember member = project.findProjectMember(jwtUserId);

        SprintBacklogItem backlogItem = scrumDetails.addBlockerToStory(backlogItemId, member, commentRequest);

        projectRepository.save(project);

        var event = new ProjectActivityEvents.BlockerCreatedEvent(
                projectId, member.getId(), member.getUser().getUsername(),
                backlogItem.getUserStory().getId(), backlogItem.getUserStory().getTitle(), commentRequest.getMessage());
        applicationEventPublisher.publishEvent(event);

        return sprintBacklogItemMapper.toResponse(backlogItem);
    }

    @Override
    @Transactional
    public void removeBlockerFromStory(Long projectId, Long backlogItemId, Long blockerId, String jwtUserId) {
        Project project = findProjectById(projectId);
        ScrumDetails scrumDetails = project.getScrumDetails();
        ProjectMember member = project.findProjectMember(jwtUserId);
        SprintBacklogItem backlogItem = scrumDetails.findActiveSprint().findBacklogItemById(backlogItemId);

        member.canDeleteCommentsAndBlocker();

        Comment removedComment = scrumDetails.removeCommentFromStory(backlogItemId, blockerId);

        projectRepository.save(project);

        var event = new ProjectActivityEvents.BlockerResolvedEvent(
                projectId, member.getId(), member.getUser().getUsername(),
                backlogItem.getUserStory().getId(), backlogItem.getUserStory().getTitle(), removedComment.getMessage());
        applicationEventPublisher.publishEvent(event);
    }

    private Project findProjectById(Long projectId) {
        return projectRepository.findById(projectId)
                .orElseThrow(() -> new ProjectNotFoundException(projectId));
    }
}
