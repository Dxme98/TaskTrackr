package com.dev.tasktrackr.project.api;

import com.dev.tasktrackr.project.api.dtos.ProjectDto;
import com.dev.tasktrackr.project.service.ProjectService;
import com.dev.tasktrackr.shared.ErrorResponse;
import com.dev.tasktrackr.shared.ValidationErrorResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/v1/projects")
@PreAuthorize("hasAuthority('USER')")
//@Validated // BEST PRACTICE: Enable validation
@Tag(name = "Projects", description = "Project management operations")
@Slf4j
public class ProjectController {
    private final ProjectService projectService;

    public ProjectController(ProjectService projectService) {
        this.projectService = projectService;
    }


    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create a new project",
            description = "Creates a new project with the authenticated user as owner")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Project created successfully",
                    content = @Content(schema = @Schema(implementation = ProjectDto.Response.class))),
    })
    public ResponseEntity<ProjectDto.Response> createProject(@AuthenticationPrincipal Jwt jwt, @Valid @RequestBody ProjectDto.Request request) {
        log.info("Creating project request from user: {}", jwt.getClaimAsString("preferred_username"));


        ProjectDto.Response response = projectService.createProject(jwt.getClaim("sub"), request);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
