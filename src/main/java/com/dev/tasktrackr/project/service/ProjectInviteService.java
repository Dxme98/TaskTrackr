package com.dev.tasktrackr.project.service;

import com.dev.tasktrackr.project.api.dtos.request.ProjectInviteRequest;
import com.dev.tasktrackr.project.api.dtos.response.ProjectInviteResponseDto;
import com.dev.tasktrackr.project.domain.ProjectInviteStatus;
import com.dev.tasktrackr.project.enums.InviteStatusEnum;

public interface ProjectInviteService {
    ProjectInviteResponseDto createProjectInvite(ProjectInviteRequest request, String senderUserId);
    ProjectInviteResponseDto updateProjectInvite(InviteStatusEnum newInviteStatus, String receiverUserId);
}
