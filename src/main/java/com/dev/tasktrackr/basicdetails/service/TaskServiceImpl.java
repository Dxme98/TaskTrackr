package com.dev.tasktrackr.basicdetails.service;


import static com.dev.tasktrackr.activity.ProjectActivityEvents.TaskCompletedEvent;
import static com.dev.tasktrackr.activity.ProjectActivityEvents.TaskCreatedEvent;
import static com.dev.tasktrackr.activity.ProjectActivityEvents.TaskDeletedEvent;
import com.dev.tasktrackr.basicdetails.api.dtos.mapper.TaskMapper;
import com.dev.tasktrackr.basicdetails.api.dtos.request.CreateTaskRequest;
import com.dev.tasktrackr.basicdetails.api.dtos.response.TaskResponseDto;
import com.dev.tasktrackr.basicdetails.domain.BasicDetails;
import com.dev.tasktrackr.project.domain.ProjectMember;
import com.dev.tasktrackr.basicdetails.domain.Task;
import com.dev.tasktrackr.project.domain.enums.Status;
import com.dev.tasktrackr.basicdetails.repository.TaskRepository;
import com.dev.tasktrackr.project.service.ProjectAccessService;
import com.dev.tasktrackr.shared.exception.custom.AccessDeniedExceptions.ProjectMemberNotAllowedToCompleteTaskException;
import com.dev.tasktrackr.shared.exception.custom.NotFoundExceptions.TaskNotFoundException;
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
    private final ProjectAccessService projectAccessService;
    private final TaskMapper taskMapper;
    private final TaskRepository taskRepository;
    private final ApplicationEventPublisher applicationEventPublisher;

    @Override
    @Transactional
    public TaskResponseDto createTask(Long projectId, CreateTaskRequest createTaskRequest, String jwtUserId) {
        BasicDetails basicDetails = projectAccessService.findProjectById(projectId).getBasicDetails();
        ProjectMember taskCreator = projectAccessService.findProjectMemberWithPermissionsRolesAndUser(jwtUserId, projectId);

        taskCreator.canCreateTask();

        Set<ProjectMember> assignedMembers = projectAccessService.findProjectMembers(createTaskRequest.getAssignedToMemberIds());
        Task createdTask = Task.create(createTaskRequest, basicDetails, taskCreator, assignedMembers);

        Task perisistedTask = taskRepository.save(createdTask);

        var event = new TaskCreatedEvent(projectId, taskCreator.getId(), taskCreator.getUser().getUsername(), perisistedTask.getId(), perisistedTask.getTitle());
        applicationEventPublisher.publishEvent(event);

        return taskMapper.toResponse(perisistedTask);
    }

    @Override
    @Transactional
    public TaskResponseDto completeTask(Long projectId, Long taskId, String jwtUserId) {
        ProjectMember member = projectAccessService.findProjectMemberWithPermissionsRolesAndUser(jwtUserId, projectId);
        Task taskToComplete = findTaskById(taskId);

        memberIsAllowedToCompleteTask(taskId, member.getId());

        taskToComplete.complete();

        var event = new TaskCompletedEvent(projectId, member.getId(), member.getUser().getUsername(), taskToComplete.getId(), taskToComplete.getTitle());
        applicationEventPublisher.publishEvent(event);

        return taskMapper.toResponse(taskToComplete);
    }

    @Override
    @Transactional
    public void deleteTask(Long projectId, Long taskId, String jwtUserId) {
        ProjectMember requestMember = projectAccessService.findProjectMemberWithPermissionsRolesAndUser(jwtUserId, projectId);
        Task taskToDelete = findTaskById(taskId);

        requestMember.canDeleteTask();

        taskRepository.deleteById(taskId);

        var event = new TaskDeletedEvent(projectId, requestMember.getId(), requestMember.getUser().getUsername(), taskToDelete.getId(), taskToDelete.getTitle());
        applicationEventPublisher.publishEvent(event);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<TaskResponseDto> findAllTasks(Long projectId, Pageable pageable, String jwtUserId, boolean assigned, Status status) {
        projectAccessService.checkProjectMemberShip(projectId, jwtUserId);

        if(status != null) {
            Page<Task> tasks =  taskRepository.findAllByStatus(projectId, status, pageable);
            return tasks.map(taskMapper::toResponse);
        }

        if(assigned) {
            Page<Task> tasks =  taskRepository.findAllTasksByAssignedUserId(projectId, jwtUserId, pageable);
            return tasks.map(taskMapper::toResponse);
        }

        Page<Task> tasks = taskRepository.findAllByProjectId(projectId, pageable);
        return tasks.map(taskMapper::toResponse);
    }

    /** Helper Methods */

    private void memberIsAllowedToCompleteTask(Long taskId, Long memberId) {
        if(!taskRepository.isMemberAllowedToCompleteTask(taskId, memberId)) throw new ProjectMemberNotAllowedToCompleteTaskException(memberId);
    }

    private Task findTaskById(Long taskId) {
        return taskRepository.findById(taskId)
                .orElseThrow(() -> new TaskNotFoundException(taskId));
    }
}
