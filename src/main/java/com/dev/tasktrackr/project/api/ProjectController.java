package com.dev.tasktrackr.project.api;

import com.dev.tasktrackr.project.api.dtos.ProjectDto;
import com.dev.tasktrackr.project.service.ProjectService;
import com.dev.tasktrackr.shared.api.annotation.ApiErrorResponses;
import com.dev.tasktrackr.user.UserId;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/projects")
@Tag(name = "Projects", description = "Project management operations")
@Slf4j
@ApiErrorResponses.CommonErrors
public class ProjectController {
    private final ProjectService projectService;

    public ProjectController(ProjectService projectService) {
        this.projectService = projectService;
    }

    @PostMapping
    @Operation(summary = "Create a new project", description = "Creates a new project with the authenticated user as owner")
    @ApiResponse(responseCode = "201", description = "Project created successfully",
            content = @Content(schema = @Schema(implementation = ProjectDto.Response.class)))
    @ApiErrorResponses.BadRequest
    public ResponseEntity<ProjectDto.Response> createProject(
            @AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody ProjectDto.Request request) {

        log.info("Creating project request from user: {}", jwt.getClaimAsString("preferred_username"));

        UserId userId = extractUserId(jwt.getClaim("sub"));
        ProjectDto.Response response = projectService.createProject(userId, request);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    @Operation(summary = "Get all projects where user is part of", description = "Returns a list of every project where User is part of")
    @ApiResponse(responseCode = "200", description = "Projects loaded successfully",
            content = @Content(array = @ArraySchema(schema = @Schema(implementation = ProjectDto.Response.class))))
    public ResponseEntity<List<ProjectDto.Response>> findProjectsByUserId(@AuthenticationPrincipal Jwt jwt) {
        UserId userId = extractUserId(jwt.getClaim("sub"));

        List<ProjectDto.Response> response = projectService.findProjectsByUserId(userId);

        return ResponseEntity.status(HttpStatus.OK).body(response);
    }


    private UserId extractUserId(String userId) {
        return new UserId(userId);
    }
}
