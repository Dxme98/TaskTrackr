package com.dev.tasktrackr.project.service;

import com.dev.tasktrackr.project.api.dtos.request.ProjectInviteRequest;
import com.dev.tasktrackr.project.api.dtos.response.ProjectInviteResponseDto;
import com.dev.tasktrackr.project.domain.ProjectInviteStatus;
import com.dev.tasktrackr.project.enums.InviteStatusEnum;
import org.springframework.stereotype.Service;

@Service
public class ProjectInviteServiceImpl implements ProjectInviteService {
    @Override
    public ProjectInviteResponseDto createProjectInvite(ProjectInviteRequest request, String senderUserId) {
        return null;
    }

    @Override
    public ProjectInviteResponseDto updateProjectInvite(InviteStatusEnum newInviteStatus, String receiverUserId) {
        return null;
    }
}
