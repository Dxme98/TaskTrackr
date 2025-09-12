package com.dev.tasktrackr.project.api.controller;

import com.dev.tasktrackr.project.api.dtos.response.ProjectMemberDto;
import com.dev.tasktrackr.project.api.dtos.request.CreateProjectRoleRequest;
import com.dev.tasktrackr.project.api.dtos.request.RenameRoleRequest;
import com.dev.tasktrackr.project.api.dtos.response.PageResponse;
import com.dev.tasktrackr.project.api.dtos.response.ProjectRolePageResponse;
import com.dev.tasktrackr.project.api.dtos.response.ProjectRoleResponse;
import com.dev.tasktrackr.project.service.ProjectRoleService;
import com.dev.tasktrackr.shared.api.annotation.ApiErrorResponses;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/projects")
@RequiredArgsConstructor
@Slf4j
@ApiErrorResponses.SecuredResourceEndpoint
public class ProjectRoleController {

    private final ProjectRoleService projectRoleService;

    @PostMapping("/{projectId}/roles")
    @Operation(summary = "Create a new role in a project", description = "Creates a new project role with the given permissions")
    @ApiResponse(responseCode = "201", description = "Role created successfully",
            content = @Content(schema = @Schema(implementation = ProjectRoleResponse.class)))
    @ApiErrorResponses.Conflict
    public ResponseEntity<ProjectRoleResponse> createRole(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable(name = "projectId") Long projectId,
            @Valid @RequestBody CreateProjectRoleRequest createProjectRoleRequest) {

        log.info("User {} creates new role in project {}", jwt.getClaimAsString("preferred_username"), projectId);

        String userId = jwt.getClaim("sub");
        ProjectRoleResponse response = projectRoleService.createProjectRole(userId, createProjectRoleRequest, projectId);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{projectId}/roles")
    @Operation(
            summary = "Get all roles in a project",
            description = "Retrieves a paginated list of all roles in the specified project"
    )
    @ApiResponse(
            responseCode = "200",
            description = "Roles retrieved successfully",
            content = @Content(schema = @Schema(implementation = ProjectRolePageResponse.class))
    )
    public ResponseEntity<PageResponse<ProjectRoleResponse>> getAllRoles(@AuthenticationPrincipal Jwt jwt,
                                                                         @PathVariable(name = "projectId") Long projectId,
                                                                         @RequestParam(name = "size", defaultValue = "10") int size,
                                                                         @RequestParam(name = "page", defaultValue = "0") int page) {
        log.info("User {} requests roles in project {}", jwt.getClaimAsString("preferred_username"), projectId);
        PageRequest pr = PageRequest.of(page, size);

        String userId = jwt.getClaim("sub");
        Page<ProjectRoleResponse> response = projectRoleService.getAllRoles(userId,  pr, projectId);

        return ResponseEntity.ok(PageResponse.from(response));
    }

    @DeleteMapping("/{projectId}/roles/{roleId}")
    @Operation(summary = "Delete a role from a project", description = "Deletes a specific role by its ID from a project")
    @ApiResponse(responseCode = "204", description = "Role deleted successfully")
    public ResponseEntity<Void> deleteRole(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable(name = "projectId") Long projectId,
            @PathVariable(name = "roleId") int roleId) {

        log.info("User {} deletes role {} from project {}", jwt.getClaimAsString("preferred_username"), roleId, projectId);

        String userId = jwt.getClaim("sub");
        projectRoleService.deleteProjectRole(userId, projectId, roleId);

        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{projectId}/roles/{roleId}/assign/{memberId}")
    @Operation(summary = "Assign a role to a member", description = "Assigns an existing project role to a project member")
    @ApiResponse(responseCode = "200", description = "Role assigned successfully",
            content = @Content(schema = @Schema(implementation = ProjectMemberDto.class)))
    public ResponseEntity<ProjectMemberDto> assignRole(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable(name = "projectId") Long projectId,
            @PathVariable(name = "roleId") int roleId,
            @PathVariable(name = "memberId") Long memberId) {

        log.info("User {} assigns role {} to member {} in project {}",
                jwt.getClaimAsString("preferred_username"), roleId, memberId, projectId);

        String userId = jwt.getClaim("sub");
        ProjectMemberDto response = projectRoleService.assignRole(userId, roleId, memberId, projectId);

        return ResponseEntity.ok(response);
    }

    @PutMapping("/{projectId}/roles/{roleId}/rename")
    @Operation(summary = "Rename a project role", description = "Renames an existing project role by its ID")
    @ApiResponse(responseCode = "200", description = "Role renamed successfully",
            content = @Content(schema = @Schema(implementation = ProjectRoleResponse.class)))
    @ApiErrorResponses.Conflict
    public ResponseEntity<ProjectRoleResponse> renameRole(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable(name = "projectId") Long projectId,
            @PathVariable(name = "roleId") int roleId,
            @Valid @RequestBody RenameRoleRequest renameRoleRequest) {

        log.info("User {} renames role {} in project {}", jwt.getClaimAsString("preferred_username"), roleId, projectId);

        String userId = jwt.getClaim("sub");
        ProjectRoleResponse response = projectRoleService.renameRole(userId, renameRoleRequest.getName(), projectId, roleId);

        return ResponseEntity.ok(response);
    }

}
