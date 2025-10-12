package com.dev.tasktrackr.project.service;

import com.dev.tasktrackr.project.api.dtos.request.CreateSprintRequest;
import com.dev.tasktrackr.project.api.dtos.response.SprintResponseDto;

public interface SprintService {
    SprintResponseDto createSprint(CreateSprintRequest createSprintRequest, Long projectId, String jwtUserId);
    SprintResponseDto editSprint(CreateSprintRequest createSprintRequest, Long projectId, String jwtUserId);
    SprintResponseDto startSprint(Long sprintId, Long projectId, String jwtUserId);
    SprintResponseDto endSprint(Long sprintId, Long projectId, String jwtUserId);
}
