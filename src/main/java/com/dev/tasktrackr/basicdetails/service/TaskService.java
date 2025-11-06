package com.dev.tasktrackr.basicdetails.service;

import com.dev.tasktrackr.basicdetails.api.dtos.request.CreateTaskRequest;
import com.dev.tasktrackr.basicdetails.api.dtos.response.TaskResponseDto;
import com.dev.tasktrackr.project.domain.enums.Status;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface TaskService {
    TaskResponseDto createTask(Long projectId, CreateTaskRequest createTaskRequest,  String jwtUserId);
    TaskResponseDto completeTask(Long projectId,  Long taskId, String jwtUserId);
    void deleteTask(Long projectId,  Long taskId, String jwtUserId);
    Page<TaskResponseDto> findAllTasks(Long projectId, Pageable pageable, String jwtUserId,
                                       boolean assigned, Status status);
}
