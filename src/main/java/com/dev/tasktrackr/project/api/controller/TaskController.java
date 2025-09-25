package com.dev.tasktrackr.project.api.controller;

import com.dev.tasktrackr.project.api.dtos.request.CreateTaskRequest;
import com.dev.tasktrackr.project.api.dtos.response.PageResponse;
import com.dev.tasktrackr.project.api.dtos.response.TaskPageResponse;
import com.dev.tasktrackr.project.api.dtos.response.TaskResponseDto;
import com.dev.tasktrackr.project.domain.enums.Status;
import com.dev.tasktrackr.project.service.TaskService;
import com.dev.tasktrackr.shared.api.annotation.ApiErrorResponses;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import static org.springframework.data.jpa.repository.query.KeysetScrollSpecification.createSort;

@RestController
@RequestMapping("/api/v1/projects/{projectId}/tasks")
@RequiredArgsConstructor
@Tag(name = "Tasks", description = "Endpoints for managing tasks within a project with type BASIC")
@ApiErrorResponses.SecuredResourceEndpoint
public class TaskController {
    private final TaskService taskService;

    @PostMapping
    @Operation(summary = "Create a new task", description = "Creates a new task within a specific project.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Task created successfully",
                    content = @Content(schema = @Schema(implementation = TaskResponseDto.class)))
    })
    public ResponseEntity<TaskResponseDto> createTask(@PathVariable(name = "projectId") Long projectId,
                                                      @RequestBody @Valid CreateTaskRequest createTaskRequest,
                                                      @AuthenticationPrincipal Jwt jwt) {
        String jwtUserId = jwt.getClaim("sub");
        TaskResponseDto response = taskService.createTask(projectId, createTaskRequest, jwtUserId);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/{taskId}/complete")
    @Operation(summary = "Complete a task", description = "Marks a task as complete.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Task completed successfully",
                    content = @Content(schema = @Schema(implementation = TaskResponseDto.class)))
    })
    public ResponseEntity<TaskResponseDto> completeTask(@PathVariable(name = "projectId") Long projectId,
                                                        @PathVariable(name = "taskId") Long taskId,
                                                        @AuthenticationPrincipal Jwt jwt) {
        String jwtUserId = jwt.getClaim("sub");
        TaskResponseDto response = taskService.completeTask(projectId, taskId, jwtUserId);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{taskId}")
    @Operation(summary = "Delete a task", description = "Deletes a task from a project.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Task deleted successfully"),
    })
    public ResponseEntity<Void> deleteTask(@PathVariable(name = "projectId") Long projectId,
                                           @PathVariable(name = "taskId") Long taskId,
                                           @AuthenticationPrincipal Jwt jwt) {
        String jwtUserId = jwt.getClaim("sub");
        taskService.deleteTask(projectId, taskId, jwtUserId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping
    @Operation(summary = "Get all tasks",
            description = "Retrieves a paginated and sortable list of tasks for a project. Can be filtered by status or assigned member.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Tasks retrieved successfully",
                    content = @Content(schema = @Schema(implementation = TaskPageResponse.class)))
    })
    public ResponseEntity<PageResponse<TaskResponseDto>> findAllTasks(@PathVariable(name = "projectId") Long projectId,
                                                                      @RequestParam(name = "page", defaultValue = "0") int page,
                                                                      @RequestParam(name = "size", defaultValue = "10") int size,
                                                                      @RequestParam(name = "status", required = false) Status status,
                                                                      @RequestParam(name = "assigned", required = false) boolean assigned,
                                                                      @RequestParam(name = "sortBy", defaultValue = "createdAt") String sortBy,
                                                                      @RequestParam(name = "sortDir", defaultValue = "asc") String sortDir,
                                                                      @AuthenticationPrincipal Jwt jwt) {
        Sort sort = Sort.by(Sort.Direction.fromString(sortDir), sortBy);
        Pageable pr = PageRequest.of(page, size, sort);
        String jwtUserId = jwt.getClaim("sub");

        Page<TaskResponseDto> response = taskService.findAllTasks(projectId, pr, jwtUserId, assigned, status);

        return ResponseEntity.ok(PageResponse.from(response));
    }
}
