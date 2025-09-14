package com.dev.tasktrackr.project.service;

import com.dev.tasktrackr.project.api.dtos.request.CreateTaskRequest;
import com.dev.tasktrackr.project.api.dtos.response.TaskResponseDto;
import com.dev.tasktrackr.project.domain.enums.Status;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

public interface TaskService {
    TaskResponseDto createTask(Long projectId, CreateTaskRequest createTaskRequest,  String jwtUserId);
    TaskResponseDto completeTask(Long projectId,  Long taskId, String jwtUserId);
    void deleteTask(Long projectId,  Long taskId, String jwtUserId);
    Page<TaskResponseDto> findAllTasks(Long projectId, Pageable pageable, String jwtUserId,
                                       Long memberId, Status status);
}
