package com.dev.tasktrackr.project.service;

import com.dev.tasktrackr.project.api.dtos.ProjectInviteMapper;
import com.dev.tasktrackr.project.api.dtos.request.ProjectInviteRequest;
import com.dev.tasktrackr.project.api.dtos.response.ProjectInviteResponseDto;
import com.dev.tasktrackr.project.domain.Project;
import com.dev.tasktrackr.project.domain.ProjectInvite;
import com.dev.tasktrackr.project.domain.enums.ProjectInviteStatus;
import com.dev.tasktrackr.project.repository.ProjectInviteQueryRepository;
import com.dev.tasktrackr.project.repository.ProjectMemberQueryRepository;
import com.dev.tasktrackr.project.repository.ProjectRepository;
import com.dev.tasktrackr.shared.exception.custom.*;
import com.dev.tasktrackr.user.UserEntity;
import com.dev.tasktrackr.user.UserRepository;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.Hibernate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.Set;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProjectInviteServiceImpl implements ProjectInviteService {
    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;
    private final ProjectInviteMapper projectInviteMapper;
    private final ProjectInviteQueryRepository projectInviteQueryRepository;


    @Override
    @Transactional
    public ProjectInviteResponseDto createProjectInvite(ProjectInviteRequest request, String senderId) {
        Project project = projectRepository.findProjectWithInvitesAndMember(request.getProjectId())
                .orElseThrow(() -> new ProjectNotFoundException(request.getProjectId()));

        validateInviteCreation(project, request.getReceiverId(), senderId);

        UserEntity sender =  findUserById(senderId);
        UserEntity receiver = findUserById(request.getReceiverId());

        project.createInvite(sender,  receiver);
        Project savedProject = projectRepository.save(project);

        ProjectInvite persistedInvite = project.findCreatedInvite();
        log.info("Invite to Project {} created successfully for user: {}", savedProject.getName(), receiver.getUsername());

       return projectInviteMapper.toResponse(persistedInvite);
    }


    @Override
    @Transactional
    public ProjectInviteResponseDto acceptProjectInvite(String receiverId, Long inviteId) {
        ProjectInvite invite = findProjectInviteWithRelations(inviteId); // throws if not found
        validateInviteResponse(receiverId, invite);

        invite.accept();

        Project project = invite.getProject();
        UserEntity receiver =  invite.getReceiver();

        project.addMember(receiver);
        Project savedProject = projectRepository.save(project);

        log.info("Invite to Project {} accepted successfully for user: {}", savedProject.getName(), receiver.getUsername());

        return projectInviteMapper.toResponse(invite);
    }

    @Override
    @Transactional
    public ProjectInviteResponseDto declineProjectInvite(String receiverId, Long inviteId) {
        ProjectInvite invite = findProjectInviteWithRelations(inviteId);
        validateInviteResponse(receiverId, invite);

        invite.decline();

        Project project = invite.getProject(); // already in context
        Project savedProject = projectRepository.save(project);

        log.info("Invite to Project {} declined successfully for user: {}", savedProject.getName(), invite.getReceiver().getUsername());

        return projectInviteMapper.toResponse(invite);
    }

    @Override
    public Page<ProjectInviteResponseDto> findAllPendingInvitesByUserId(String userId, PageRequest pageRequest) {
        return projectInviteQueryRepository
                .findProjectInvitesByReceiverIdAndInviteStatus(userId, ProjectInviteStatus.PENDING, pageRequest);
    }

    private void validateInviteCreation(Project project, String receiverId, String senderId) {
        // Check if Einladung bereits existiert
        boolean inviteExists = project.getProjectInvites().stream()
                .anyMatch(invite -> invite.getReceiver().getId().equals(receiverId));
        if (inviteExists) {
            throw new ProjectInviteAlreadyExistsException(receiverId, project.getId());
        }

        // Check if User already part of project
        boolean receiverIsMember = project.getProjectMembers().stream()
                .anyMatch(member -> member.getUser().getId().equals(receiverId));
        if (receiverIsMember) {
            throw new UserAlreadyPartOfProjectException(receiverId, project.getId());
        }

        // Check if Sender is part of project
        boolean senderIsMember = project.getProjectMembers().stream()
                .anyMatch(member -> member.getUser().getId().equals(senderId));
        if (!senderIsMember) {
            throw new UserNotProjectMemberException(senderId);
        }
    }

    private void validateInviteResponse(String jwtUserId, ProjectInvite invite) {
        String receiverId = invite.getReceiver().getId();
        Long inviteId = invite.getId();
        Project project = invite.getProject();

        // receiverId muss mit jwtUserId übereinstimmen
        if (!jwtUserId.equals(receiverId)) {
            throw new UnauthorizedInviteHandleAcception(jwtUserId, receiverId);
        }

        // Status muss PENDING sein
        if (invite.getInviteStatus() != ProjectInviteStatus.PENDING) {
            throw new InviteIsNotPendingException(inviteId);
        }

        // Prüfen ob User bereits Teil des Projekts ist
        boolean receiverIsMember = project.getProjectMembers().stream()
                .anyMatch(member -> member.getUser().getId().equals(receiverId));

        if (receiverIsMember) {
            throw new UserAlreadyPartOfProjectException(receiverId, project.getId());
        }
    }


    // Loads ProjectInvite with relevant Entites: Sender, Receiver and Project
    private ProjectInvite findProjectInviteWithRelations(Long inviteId) {
        return projectInviteQueryRepository.findByIdWithRelations(inviteId)
                .orElseThrow(() -> new ProjectInviteNotFound(inviteId));
    }

    UserEntity findUserById(String userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));
    }
 }
