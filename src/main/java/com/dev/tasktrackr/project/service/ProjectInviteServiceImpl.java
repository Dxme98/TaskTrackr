package com.dev.tasktrackr.project.service;

import static com.dev.tasktrackr.activity.ProjectActivityEvents.UserJoinedProjectEvent;

import com.dev.tasktrackr.project.api.dtos.mapper.ProjectInviteMapper;
import com.dev.tasktrackr.project.api.dtos.request.ProjectInviteRequest;
import com.dev.tasktrackr.project.api.dtos.response.ProjectInviteResponseDto;
import com.dev.tasktrackr.project.domain.Project;
import com.dev.tasktrackr.project.domain.ProjectInvite;
import com.dev.tasktrackr.project.domain.ProjectMember;
import com.dev.tasktrackr.project.domain.ProjectRole;
import com.dev.tasktrackr.project.domain.enums.ProjectInviteStatus;
import com.dev.tasktrackr.project.domain.enums.RoleType;
import com.dev.tasktrackr.project.repository.ProjectInviteRepository;
import com.dev.tasktrackr.project.repository.ProjectMemberRepository;
import com.dev.tasktrackr.project.repository.ProjectRoleRepository;
import com.dev.tasktrackr.shared.exception.custom.ConflictExceptions.ProjectInviteAlreadyExistsException;
import com.dev.tasktrackr.shared.exception.custom.ConflictExceptions.UserAlreadyPartOfProjectException;
import com.dev.tasktrackr.shared.exception.custom.NotFoundExceptions.*;
import com.dev.tasktrackr.user.domain.UserEntity;
import com.dev.tasktrackr.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProjectInviteServiceImpl implements ProjectInviteService {
    private final UserRepository userRepository;
    private final ProjectInviteMapper projectInviteMapper;
    private final ProjectInviteRepository projectInviteRepository;
    private final ApplicationEventPublisher applicationEventPublisher;
    private final ProjectAccessService projectAccessService;
    private final ProjectRoleRepository projectRoleRepository;
    private final ProjectMemberRepository projectMemberRepository;


    @Override
    @Transactional
    public ProjectInviteResponseDto createProjectInvite(ProjectInviteRequest request, String senderId, Long projectId) {
        Project project = projectAccessService.findProjectById(projectId);
        ProjectMember senderMember = projectAccessService.findProjectMember(senderId, projectId);
        UserEntity receiverUser = findUserByUsername(request.getReceiverUsername());

        senderMember.canInviteUser();

        validateInviteCreation(projectId, receiverUser);

        ProjectInvite invite = ProjectInvite.createInvite(senderMember.getUser(), receiverUser, project);
        projectInviteRepository.save(invite);

        return projectInviteMapper.toResponse(invite);
    }

    @Override
    @Transactional
    public ProjectInviteResponseDto acceptProjectInvite(String receiverId, Long inviteId) {
        ProjectInvite invite = findProjectInviteWithRelations(inviteId);
        UserEntity receiver = userRepository.findById(receiverId).orElseThrow(() -> new UserNotFoundException(receiverId));
        Project project = invite.getProject();

        invite.accept(receiverId);

        ProjectRole baseRoleOfProject = findBaseRoleOfProject(project.getId());
        ProjectMember newProjectMember = ProjectMember.createMember(receiver, project, baseRoleOfProject);

        projectMemberRepository.save(newProjectMember);

        var event = new UserJoinedProjectEvent(project.getId(), newProjectMember.getId(), newProjectMember.getUser().getUsername());
        applicationEventPublisher.publishEvent(event);

        return projectInviteMapper.toResponse(invite);
    }

    @Override
    @Transactional
    public void declineProjectInvite(String receiverId, Long inviteId) {
        ProjectInvite invite = findProjectInviteWithRelations(inviteId);

        invite.decline(receiverId);

        projectInviteRepository.delete(invite); // delete to enable reinvite
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ProjectInviteResponseDto> findAllPendingInvitesByUserId(String userId, PageRequest pageRequest) {
        return projectInviteRepository
                .findProjectInvitesByReceiverIdAndInviteStatus(userId, ProjectInviteStatus.PENDING, pageRequest);
    }


    /** Helper Methods */
    private ProjectInvite findProjectInviteWithRelations(Long inviteId) {
        return projectInviteRepository.findByIdWithRelations(inviteId)
                .orElseThrow(() -> new ProjectInviteNotFound(inviteId));
    }


    private UserEntity findUserByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new UserNotFoundException(username));
    }

    private void validateInviteCreation(Long projectId, UserEntity receiverUser) {
        if(projectInviteRepository.existsByProjectIdAndReceiverIdAndInviteStatus(projectId, receiverUser.getId(), ProjectInviteStatus.PENDING)) throw new ProjectInviteAlreadyExistsException(receiverUser.getId(), projectId);
        if(projectMemberRepository.existsByUserIdAndProjectId(receiverUser.getId(), projectId)) throw new UserAlreadyPartOfProjectException(receiverUser.getId(), projectId);
    }

    private ProjectRole findBaseRoleOfProject(Long projectId) {
        return projectRoleRepository.findProjectRoleByProjectIdAndRoleType(projectId, RoleType.BASE)
                .orElseThrow(() -> new RoleNotFoundException("Keine Base Role für Project gefunden"));
    }
 }
