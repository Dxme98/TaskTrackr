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
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.Hibernate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
    public ProjectInviteResponseDto createProjectInvite(ProjectInviteRequest request, String senderId, Long projectId) {
        Project project = projectRepository.findProjectWithInvitesAndMember(projectId)
                .orElseThrow(() -> new ProjectNotFoundException(projectId));

        UserEntity sender =  findUserById(senderId);
        UserEntity receiver = findUserById(request.getReceiverId());

        project.createInvite(sender,  receiver);
        Project savedProject = projectRepository.save(project);


        log.info("Invite to Project {} created successfully for user: {}", savedProject.getName(), receiver.getUsername());

        ProjectInvite persistedInvite = savedProject.getProjectInvites().get(savedProject.getProjectInvites().size()-1);

       return projectInviteMapper.toResponse(persistedInvite);
    }


    @Override
    @Transactional
    public ProjectInviteResponseDto acceptProjectInvite(String receiverId, Long inviteId) {
        ProjectInvite invite = findProjectInviteWithRelations(inviteId);

        invite.accept(receiverId);

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

        invite.decline(receiverId);

        Project project = invite.getProject(); // already in context
        Project savedProject = projectRepository.save(project);

        log.info("Invite to Project {} declined successfully for user: {}", savedProject.getName(), invite.getReceiver().getUsername());

        return projectInviteMapper.toResponse(invite);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ProjectInviteResponseDto> findAllPendingInvitesByUserId(String userId, PageRequest pageRequest) {
        return projectInviteQueryRepository
                .findProjectInvitesByReceiverIdAndInviteStatus(userId, ProjectInviteStatus.PENDING, pageRequest);
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
