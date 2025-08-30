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
    private final ProjectMemberQueryRepository projectMemberQueryRepository;
    private final EntityManager em;


    @Override
    @Transactional
    public ProjectInviteResponseDto createProjectInvite(ProjectInviteRequest request, String senderId) {
        validateInviteCreation(request.getProjectId(), request.getReceiverId(), senderId);

        Project project = findProjectById(request.getProjectId());
        UserEntity sender =  findUserById(senderId);
        UserEntity receiver = findUserById(request.getReceiverId());

        project.createInvite(sender,  receiver, project);
        Project savedProject = projectRepository.save(project);


        // In-Memory Operation - keine extra Query
        ProjectInvite persistedInvite = savedProject.getProjectInvites().stream()
                .filter(invite -> invite.getReceiver().getId().equals(receiver.getId()))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Persisted invite not found in aggregate")); // Sollte nie passieren


        log.info("Invite to Project {} created successfully for user: {}", savedProject.getName(), receiver.getUsername());

       return projectInviteMapper.toResponse(persistedInvite);
    }


    @Override
    @Transactional
    public ProjectInviteResponseDto acceptProjectInvite(String receiverId, Long inviteId) {
        ProjectInvite invite = findProjectInviteWithRelations(inviteId); // throws if not found
        validateInviteResponse(receiverId, invite);

        invite.accept();
        Project project = invite.getProject(); // already in context
        UserEntity receiver =  invite.getReceiver(); // already in context

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

    private void validateInviteCreation(Long projectId, String receiverId, String senderId) {
        // Check if einladung bereits existiert
        if(projectInviteQueryRepository.existsByReceiverIdAndProjectId(receiverId, projectId)) {
            throw new ProjectInviteAlreadyExistsException(receiverId, projectId);
        }

        // Check if user is already part of project
        if(projectMemberQueryRepository.existsByUserIdAndProjectId(receiverId, projectId)) {
            throw new UserAlreadyPartOfProjectException(receiverId, projectId);
        }

        // Check if sender  is  part of project
        if(!projectMemberQueryRepository.existsByUserIdAndProjectId(senderId, projectId)) {
            throw new UserNotProjectMemberException(senderId);
        }
    }

    private void validateInviteResponse( String jwtUserId, ProjectInvite invite) {
        String receiverId = invite.getReceiver().getId();
        Long inviteId = invite.getId();
        Long projectId = invite.getProject().getId();

        // receiverId = jwtUserId
        if(!jwtUserId.equals(receiverId)) {
            throw new UnauthorizedInviteHandleAcception(jwtUserId, receiverId);
        }

        // Check if Status == pending
        if(invite.getInviteStatus() != ProjectInviteStatus.PENDING) {
            throw new InviteIsNotPendingException(inviteId);
        }

        // Check if user is already part of project
        if(projectMemberQueryRepository.existsByUserIdAndProjectId(receiverId, projectId)) {
            throw new UserAlreadyPartOfProjectException(receiverId, projectId);
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


    Project findProjectById(Long id) {
        return projectRepository.findById(id)
                .orElseThrow(() -> new ProjectNotFoundException(id));
    }
 }
