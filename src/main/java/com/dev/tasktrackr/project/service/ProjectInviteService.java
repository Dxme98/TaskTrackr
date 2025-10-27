package com.dev.tasktrackr.project.service;

import com.dev.tasktrackr.project.api.dtos.request.ProjectInviteRequest;
import com.dev.tasktrackr.project.api.dtos.response.ProjectInviteResponseDto;
import com.dev.tasktrackr.project.domain.enums.ProjectInviteStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

public interface ProjectInviteService {
    ProjectInviteResponseDto createProjectInvite(ProjectInviteRequest request, String senderUserId, Long projectId);
    ProjectInviteResponseDto acceptProjectInvite(String receiverId, Long inviteId);
    void declineProjectInvite(String receiverId, Long inviteId);
    Page<ProjectInviteResponseDto> findAllPendingInvitesByUserId(String userId, PageRequest pageRequest);
}
