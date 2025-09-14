package com.dev.tasktrackr.project.service;

import com.dev.tasktrackr.project.api.dtos.mapper.TaskMapper;
import com.dev.tasktrackr.project.api.dtos.request.CreateTaskRequest;
import com.dev.tasktrackr.project.api.dtos.response.TaskResponseDto;
import com.dev.tasktrackr.project.domain.BasicDetails;
import com.dev.tasktrackr.project.domain.Project;
import com.dev.tasktrackr.project.domain.ProjectMember;
import com.dev.tasktrackr.project.domain.Task;
import com.dev.tasktrackr.project.repository.ProjectRepository;
import com.dev.tasktrackr.shared.exception.custom.NotFoundExceptions.ProjectNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;

@Service
@RequiredArgsConstructor
public class TaskServiceImpl implements TaskService {
    private final ProjectRepository projectRepository;
    private final TaskMapper taskMapper;

    @Override
    @Transactional
    public TaskResponseDto createTask(Long projectId, CreateTaskRequest createTaskRequest, String jwtUserId) {
        Project project = findProjectById(projectId);
        BasicDetails basicDetails = project.getBasicDetails();
        ProjectMember taskCreator = project.findProjectMember(jwtUserId);
        Set<ProjectMember> assignedMembers = project.findProjectMembers(createTaskRequest.getAssignedToMemberIds());

        Task createdTask = basicDetails.addTask(createTaskRequest, taskCreator, assignedMembers);

        projectRepository.save(project);

        return taskMapper.toResponse(createdTask);
    }

    @Override
    @Transactional
    public TaskResponseDto completeTask(Long projectId, Long taskId, String jwtUserId) {
        return null;
    }

    @Override
    @Transactional
    public void deleteTask(Long projectId, Long taskId, String jwtUserId) {

    }

    @Override
    @Transactional(readOnly = true)
    public Page<TaskResponseDto> findAllTasks(Long projectId, Pageable pageable, String jwtUserId) {
        return null;
    }

    private Project findProjectById(Long projectId) {
        return projectRepository.findById(projectId)
                .orElseThrow(() -> new ProjectNotFoundException(projectId));
    }
}
