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
import com.dev.tasktrackr.project.repository.*;
import com.dev.tasktrackr.shared.exception.custom.AccessDeniedExceptions.PermissionDeniedException;
import com.dev.tasktrackr.shared.exception.custom.NotFoundExceptions.NoActiveSprintFoundException;
import com.dev.tasktrackr.shared.exception.custom.NotFoundExceptions.SprintBacklogItemNotFoundException;
import com.dev.tasktrackr.shared.exception.custom.NotFoundExceptions.SprintSummaryItemNotFoundException;
import com.dev.tasktrackr.shared.exception.custom.NotFoundExceptions.UserStoryNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;

@Service
@RequiredArgsConstructor
public class ScrumBoardServiceImpl implements ScrumBoardService{
    private final ProjectRepository projectRepository;
    private final ScrumBoardMapper scrumBoardMapper;
    private final SprintBacklogItemMapper sprintBacklogItemMapper;
    private final ApplicationEventPublisher applicationEventPublisher;
    private final SprintQueryRepository sprintQueryRepository;
    private final ProjectMemberQueryRepository projectMemberQueryRepository;
    private final ProjectAccessService projectAccessService;
    private final SprintSummaryItemRepository sprintSummaryItemRepository;
    private final SprintBacklogItemRepository sprintBacklogItemRepository;
    private final CommentRepository commentRepository;

    @Override
    @Transactional(readOnly = true)
    public ScrumBoardResponseDto getScrumBoard(Long projectId,  String jwtUserId) {
        projectAccessService.checkProjectMemberShip(projectId, jwtUserId);

        Set<ProjectMember> projectMemberSet = projectMemberQueryRepository.findAllProjectMembersByProjectId(projectId);
        Sprint activeSprint = findActiveSprint(projectId);

        return scrumBoardMapper.toResponse(activeSprint, projectMemberSet);
    }

    @Override
    @Transactional
    public SprintBacklogItemResponse updateUserStoryStatus(Long projectId, Long backlogItemId, StoryStatus newStatus, String jwtUserId) {
        // load data
        ScrumDetails scrumDetails = projectAccessService.findProjectById(projectId).getScrumDetails();
        ProjectMember member = projectAccessService.findProjectMemberWithPermissionsRolesAndUser(jwtUserId, projectId);
        SprintBacklogItem backlogItem = findSprintBacklogItem(backlogItemId);
        SprintSummaryItem sprintSummaryItem = findSprintSummaryItem(backlogItem, backlogItem.getUserStory());

        // check permissions
        if(!member.canUpdateStoryStatus() && !backlogItem.memberIsAssigned(member)) {
            throw new PermissionDeniedException("You do not have permission to update story status.");
        }

        // update: backlogitem, summaryItem and userStory
        SprintBacklogItem updatedBacklogItem = scrumDetails.updateBacklogItemStatus(backlogItem, newStatus, backlogItem.getSprint(), sprintSummaryItem);

        // publish event
        var event = new ProjectActivityEvents.UserStoryStatusUpdatedEvent(
                projectId, member.getId(), member.getUser().getUsername(),
                updatedBacklogItem.getUserStory().getId(), updatedBacklogItem.getUserStory().getTitle(), newStatus.name());
        applicationEventPublisher.publishEvent(event);

        return sprintBacklogItemMapper.toResponse(updatedBacklogItem);
    }

    @Override
    @Transactional
    public SprintBacklogItemResponse assignMemberToStory(Long projectId, Long backlogItemId, Long memberId, String jwtUserId) {
        ScrumDetails scrumDetails = projectAccessService.findProjectById(projectId).getScrumDetails();
        ProjectMember assignedMember = projectAccessService.findProjectMember(memberId, projectId);
        ProjectMember requestedMember = projectAccessService.findProjectMemberWithPermissionsRolesAndUser(jwtUserId, projectId);
        SprintBacklogItem backlogItem = findSprintBacklogItem(backlogItemId);

        requestedMember.canAssignUserToStory();

        SprintBacklogItem updatedBacklogItem = scrumDetails.assignMemberToStory(backlogItem, assignedMember, backlogItem.getSprint());

        return sprintBacklogItemMapper.toResponse(updatedBacklogItem);
    }

    @Override
    @Transactional
    public SprintBacklogItemResponse unassignMemberFromStory(Long projectId, Long backlogItemId, Long memberId, String jwtUserId) {
        ScrumDetails scrumDetails = projectAccessService.findProjectById(projectId).getScrumDetails();
        ProjectMember assignedMember = projectAccessService.findProjectMember(memberId, projectId);
        ProjectMember requestedMember = projectAccessService.findProjectMemberWithPermissionsRolesAndUser(jwtUserId, projectId);
        SprintBacklogItem backlogItem = findSprintBacklogItem(backlogItemId);

        requestedMember.canAssignUserToStory();

        SprintBacklogItem updatedBacklogItem = scrumDetails.unassignMemberFromStory(backlogItem, assignedMember, backlogItem.getSprint());

        return sprintBacklogItemMapper.toResponse(updatedBacklogItem);
    }

    @Override
    @Transactional
    public SprintBacklogItemResponse addCommentToStory(Long projectId, Long backlogItemId, CreateCommentRequest commentRequest, String jwtUserId) {
        ScrumDetails scrumDetails = projectAccessService.findProjectById(projectId).getScrumDetails();
        ProjectMember member = projectAccessService.findProjectMember(jwtUserId, projectId);
        SprintBacklogItem backlogItem = findSprintBacklogItem(backlogItemId);

        Comment createdComment = scrumDetails.addCommentToStory(backlogItem, member, commentRequest, backlogItem.getSprint());

        commentRepository.save(createdComment);

        var event = new ProjectActivityEvents.CommentCreatedEvent(
                projectId, member.getId(), member.getUser().getUsername(),
                backlogItem.getUserStory().getId(), backlogItem.getUserStory().getTitle());
        applicationEventPublisher.publishEvent(event);

        return sprintBacklogItemMapper.toResponse(backlogItem);
    }

    @Override
    @Transactional
    public void removeCommentFromStory(Long projectId, Long backlogItemId, Long commentId, String jwtUserId) {
        Project project = projectAccessService.findProjectById(projectId);
        ScrumDetails scrumDetails = project.getScrumDetails();
        ProjectMember member = project.findProjectMember(jwtUserId);

        member.canDeleteCommentsAndBlocker();

        scrumDetails.removeCommentFromStory(backlogItemId, commentId);

        projectRepository.save(project);
    }

    @Override
    @Transactional
    public SprintBacklogItemResponse addBlockerToStory(Long projectId, Long backlogItemId, CreateCommentRequest commentRequest, String jwtUserId) {
        ScrumDetails scrumDetails = projectAccessService.findProjectById(projectId).getScrumDetails();
        ProjectMember member = projectAccessService.findProjectMember(jwtUserId, projectId);
        SprintBacklogItem backlogItem = findSprintBacklogItem(backlogItemId);

        Comment createdComment = scrumDetails.addBlockerToStory(backlogItem, member, commentRequest, backlogItem.getSprint());

        commentRepository.save(createdComment);

        var event = new ProjectActivityEvents.BlockerCreatedEvent(
                projectId, member.getId(), member.getUser().getUsername(),
                backlogItem.getUserStory().getId(), backlogItem.getUserStory().getTitle(), commentRequest.getMessage());
        applicationEventPublisher.publishEvent(event);

        return sprintBacklogItemMapper.toResponse(backlogItem);
    }

    @Override
    @Transactional
    public void removeBlockerFromStory(Long projectId, Long backlogItemId, Long blockerId, String jwtUserId) {
        Project project = projectAccessService.findProjectById(projectId);
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

    private Sprint findActiveSprint(Long projectId) {
        return sprintQueryRepository.findActiveSprintByProjectId(projectId)
                .orElseThrow(() -> new NoActiveSprintFoundException(projectId));
    }

    private SprintSummaryItem findSprintSummaryItem(SprintBacklogItem backlogItem, UserStory userStory) {
        return sprintSummaryItemRepository.findSprintSummaryItemBySprintIdAndUserStoryId(backlogItem.getSprint().getId(), userStory.getId())
                .orElseThrow(() -> new SprintSummaryItemNotFoundException(userStory.getId()));
    }

    private SprintBacklogItem findSprintBacklogItem(Long backlogItemId) {
        return sprintBacklogItemRepository.findSprintBacklogItemById(backlogItemId)
                .orElseThrow(() -> new SprintBacklogItemNotFoundException(backlogItemId));
    }
}
