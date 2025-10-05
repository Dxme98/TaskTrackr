package com.dev.tasktrackr.project.service;

import com.dev.tasktrackr.project.api.dtos.request.CreateUserStoryRequest;
import com.dev.tasktrackr.project.api.dtos.response.UserStoryResponseDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface UserStoryService {
    UserStoryResponseDto createUserStory(Long projectId, CreateUserStoryRequest createUserStoryRequest, String jwtUserId);
    Page<UserStoryResponseDto> getUserStoriesByProjectId(Long projectId, Pageable pageable, String jwtUserId);
}
