package com.dev.tasktrackr.project.service;


import static com.dev.tasktrackr.activity.ProjectActivityEvents.TaskCompletedEvent;
import static com.dev.tasktrackr.activity.ProjectActivityEvents.TaskCreatedEvent;
import static com.dev.tasktrackr.activity.ProjectActivityEvents.TaskDeletedEvent;
import com.dev.tasktrackr.project.api.dtos.mapper.TaskMapper;
import com.dev.tasktrackr.project.api.dtos.request.CreateTaskRequest;
import com.dev.tasktrackr.project.api.dtos.response.TaskResponseDto;
import com.dev.tasktrackr.project.domain.BasicDetails;
import com.dev.tasktrackr.project.domain.Project;
import com.dev.tasktrackr.project.domain.ProjectMember;
import com.dev.tasktrackr.project.domain.Task;
import com.dev.tasktrackr.project.domain.enums.Status;
import com.dev.tasktrackr.project.repository.ProjectRepository;
import com.dev.tasktrackr.project.repository.TaskQueryRepository;
import com.dev.tasktrackr.shared.exception.custom.NotFoundExceptions.ProjectNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
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
    private final TaskQueryRepository taskQueryRepository;
    private final ApplicationEventPublisher applicationEventPublisher;

    @Override
    @Transactional
    public TaskResponseDto createTask(Long projectId, CreateTaskRequest createTaskRequest, String jwtUserId) {
        Project project = findProjectById(projectId);
        BasicDetails basicDetails = project.getBasicDetails();
        ProjectMember taskCreator = project.findProjectMember(jwtUserId);

        taskCreator.canCreateTask();

        Set<ProjectMember> assignedMembers = project.findProjectMembers(createTaskRequest.getAssignedToMemberIds());

        Task createdTask = basicDetails.addTask(createTaskRequest, taskCreator, assignedMembers);

        projectRepository.save(project);

        Task perisistedTask = basicDetails.findTask(createdTask);

        var event = new TaskCreatedEvent(projectId, taskCreator.getId(), taskCreator.getUser().getUsername(), perisistedTask.getId(), perisistedTask.getTitle());
        applicationEventPublisher.publishEvent(event);

        return taskMapper.toResponse(perisistedTask);
    }

    @Override
    @Transactional
    public TaskResponseDto completeTask(Long projectId, Long taskId, String jwtUserId) {
        Project project = findProjectById(projectId);
        BasicDetails basicDetails = project.getBasicDetails();
        ProjectMember member = project.findProjectMember(jwtUserId);


        Task completedTask = basicDetails.completeTask(taskId, member.getId());

        projectRepository.save(project);

        var event = new TaskCompletedEvent(projectId, member.getId(), member.getUser().getUsername(), completedTask.getId(), completedTask.getTitle());
        applicationEventPublisher.publishEvent(event);

        return taskMapper.toResponse(completedTask);
    }

    @Override
    @Transactional
    public void deleteTask(Long projectId, Long taskId, String jwtUserId) {
        Project project = findProjectById(projectId);
        BasicDetails basicDetails = project.getBasicDetails();
        ProjectMember requestMember = project.findProjectMember(jwtUserId);

        requestMember.canDeleteTask();
        Task deletedTask = basicDetails.deleteTask(taskId);

        var event = new TaskDeletedEvent(projectId, requestMember.getId(), requestMember.getUser().getUsername(), deletedTask.getId(), deletedTask.getTitle());
        applicationEventPublisher.publishEvent(event);

        projectRepository.save(project);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<TaskResponseDto> findAllTasks(Long projectId, Pageable pageable, String jwtUserId,
                                              boolean assigned, Status status) {
        Project project = findProjectById(projectId); // need to optimize
        project.findProjectMember(jwtUserId); // throws if not member

        if(status != null) {
            Page<Task> tasks =  taskQueryRepository.findAllByStatus(projectId, status, pageable);
            return tasks.map(taskMapper::toResponse);
        }

        if(assigned) {
            Page<Task> tasks =  taskQueryRepository.findAllTasksByAssignedUserId(projectId, jwtUserId, pageable);
            return tasks.map(taskMapper::toResponse);
        }

        Page<Task> tasks = taskQueryRepository.findAllByProjectId(projectId, pageable);
        return tasks.map(taskMapper::toResponse);
    }

    private Project findProjectById(Long projectId) {
        return projectRepository.findById(projectId)
                .orElseThrow(() -> new ProjectNotFoundException(projectId));
    }
}
