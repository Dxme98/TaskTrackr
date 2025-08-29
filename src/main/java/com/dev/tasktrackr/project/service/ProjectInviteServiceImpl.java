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
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ProjectInviteServiceImpl implements ProjectInviteService {
    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;
    private final ProjectInviteMapper projectInviteMapper;
    private final ProjectInviteQueryRepository projectInviteQueryRepository;
    private final ProjectMemberQueryRepository projectMemberQueryRepository;

    @Override
    @Transactional
    public ProjectInviteResponseDto createProjectInvite(ProjectInviteRequest request, String senderJwtUserId) {
        validateInviteCreation(request.getProjectId(), request.getReceiverId(), request.getSenderId(), senderJwtUserId);

        Project project = findProjectById(request.getProjectId());
        UserEntity sender =  findUserById(senderJwtUserId);
        UserEntity receiver = findUserById(request.getReceiverId());

        ProjectInvite createdInvite = ProjectInvite.createInvite(sender,  receiver, project);
        project.addInvite(createdInvite);

        projectRepository.save(project);

        return projectInviteMapper.toResponse(createdInvite);
    }

    @Override
    public ProjectInviteResponseDto updateProjectInvite(ProjectInviteStatus newProjectInviteStatus, String receiverUserId) {
        return null;
    }


    Project findProjectById(Long id) {
        return projectRepository.findById(id).orElseThrow(() -> new ProjectNotFoundException(id));
    }

    UserEntity findUserById(String userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));
    }

    private void validateInviteCreation(Long projectId, String receiverId, String requestSenderId, String senderJwtUserId) {

        // Check if JWT Sender ID = Request Sender ID
        if(!requestSenderId.equals(senderJwtUserId)) {
            throw new UnauthorizedInviteAttemptException(senderJwtUserId, requestSenderId);
        }

        // Check if einladung bereits existiiert
        if(projectInviteQueryRepository.existsByReceiverIdAndProjectId(receiverId, projectId)) {
            throw new ProjectInviteAlreadyExistsException(receiverId, projectId);
        }

        // Check if user is already part of project
        if(projectMemberQueryRepository.existsByUserIdAndProjectId(receiverId, projectId)) {
            throw new UserAlreadyPartOfProjectException(receiverId, projectId);
        }
    }
 }
