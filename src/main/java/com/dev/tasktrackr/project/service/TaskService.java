package com.dev.tasktrackr.project.service;

import com.dev.tasktrackr.project.api.dtos.request.CreateTaskRequest;
import com.dev.tasktrackr.project.api.dtos.response.TaskResponseDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface TaskService {
    TaskResponseDto createTask(Long projectId, CreateTaskRequest createTaskRequest,  String jwtUserId);
    TaskResponseDto completeTask(Long projectId,  Long taskId, String jwtUserId);
    void deleteTask(Long projectId,  Long taskId, String jwtUserId);
    Page<TaskResponseDto> findAllTasks(Long projectId, Pageable pageable, String jwtUserId);
}
