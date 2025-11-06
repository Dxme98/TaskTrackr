package com.dev.tasktrackr.scrumdetails.service;

import com.dev.tasktrackr.scrumdetails.api.dtos.request.CreateSprintRequest;
import com.dev.tasktrackr.scrumdetails.api.dtos.response.SprintResponseDto;
import com.dev.tasktrackr.scrumdetails.domain.SprintStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface SprintService {
    SprintResponseDto createSprint(CreateSprintRequest createSprintRequest, Long projectId, String jwtUserId);
    SprintResponseDto findActiveSprint( Long projectId, String jwtUserId);
    Page<SprintResponseDto> findAllSprintsByProjectIdAndStatus(Long projectId, String jwtUserId, Pageable pageable, SprintStatus status);
    SprintResponseDto startSprint(Long sprintId, Long projectId, String jwtUserId);
    SprintResponseDto endSprint(Long sprintId, Long projectId, String jwtUserId);
}
