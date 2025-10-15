package com.dev.tasktrackr.project.service;

import com.dev.tasktrackr.project.api.dtos.request.CreateCommentRequest;
import com.dev.tasktrackr.project.api.dtos.response.ScrumBoardResponseDto;
import com.dev.tasktrackr.project.api.dtos.response.SprintBacklogItemResponse;
import com.dev.tasktrackr.project.domain.scrum.StoryStatus;

public interface ScrumBoardService {
    ScrumBoardResponseDto getScrumBoard(Long projectId, String jwtUserId);
    SprintBacklogItemResponse updateUserStoryStatus(Long projectId, Long backlogItemId, StoryStatus newStatus, String jwtUserId);
    SprintBacklogItemResponse assignMemberToStory(Long projectId, Long backlogItemId, Long memberId, String jwtUserId);
    SprintBacklogItemResponse unassignMemberFromStory(Long projectId, Long backlogItemId, Long memberId, String jwtUserId);
    SprintBacklogItemResponse addCommentToStory(Long projectId, Long backlogItemId, CreateCommentRequest commentRequest, String jwtUserId);
    void removeCommentFromStory(Long projectId, Long backlogItemId, Long commentId, String jwtUserId);
    SprintBacklogItemResponse addBlockerToStory(Long projectId, Long backlogItemId, CreateCommentRequest commentRequest, String jwtUserId);
    void removeBlockerFromStory(Long projectId, Long backlogItemId, Long blockerId, String jwtUserId);
}
