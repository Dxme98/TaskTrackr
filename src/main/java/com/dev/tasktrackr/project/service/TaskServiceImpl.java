package com.dev.tasktrackr.project.service;


import static com.dev.tasktrackr.activity.ProjectActivityEvents.TaskCompletedEvent;
import static com.dev.tasktrackr.activity.ProjectActivityEvents.TaskCreatedEvent;
import static com.dev.tasktrackr.activity.ProjectActivityEvents.TaskDeletedEvent;
import com.dev.tasktrackr.project.api.dtos.mapper.TaskMapper;
import com.dev.tasktrackr.project.api.dtos.request.CreateTaskRequest;
import com.dev.tasktrackr.project.api.dtos.response.TaskResponseDto;
import com.dev.tasktrackr.project.domain.basic.BasicDetails;
import com.dev.tasktrackr.project.domain.ProjectMember;
import com.dev.tasktrackr.project.domain.Task;
import com.dev.tasktrackr.project.domain.enums.Status;
import com.dev.tasktrackr.project.repository.TaskQueryRepository;
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
    private final TaskQueryRepository taskQueryRepository;
    private final ApplicationEventPublisher applicationEventPublisher;

    @Override
    @Transactional
    public TaskResponseDto createTask(Long projectId, CreateTaskRequest createTaskRequest, String jwtUserId) {
        BasicDetails basicDetails = projectAccessService.findProjectById(projectId).getBasicDetails();
        ProjectMember taskCreator = projectAccessService.findProjectMemberWithPermissionsRolesAndUser(jwtUserId, projectId);

        taskCreator.canCreateTask();

        Set<ProjectMember> assignedMembers = projectAccessService.findProjectMembers(createTaskRequest.getAssignedToMemberIds());
        Task createdTask = Task.create(createTaskRequest, basicDetails, taskCreator, assignedMembers);

        Task perisistedTask = taskQueryRepository.save(createdTask);

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

        taskQueryRepository.deleteById(taskId);

        var event = new TaskDeletedEvent(projectId, requestMember.getId(), requestMember.getUser().getUsername(), taskToDelete.getId(), taskToDelete.getTitle());
        applicationEventPublisher.publishEvent(event);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<TaskResponseDto> findAllTasks(Long projectId, Pageable pageable, String jwtUserId, boolean assigned, Status status) {
        projectAccessService.checkProjectMemberShip(projectId, jwtUserId);

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

    /** Helper Methods */

    private void memberIsAllowedToCompleteTask(Long taskId, Long memberId) {
        if(!taskQueryRepository.isMemberAllowedToCompleteTask(taskId, memberId)) throw new ProjectMemberNotAllowedToCompleteTaskException(memberId);
    }

    private Task findTaskById(Long taskId) {
        return taskQueryRepository.findById(taskId)
                .orElseThrow(() -> new TaskNotFoundException(taskId));
    }
}
