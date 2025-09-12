package com.dev.tasktrackr.project.api.controller;

import com.dev.tasktrackr.project.api.dtos.response.ProjectMemberDto;
import com.dev.tasktrackr.project.api.dtos.response.PageResponse;
import com.dev.tasktrackr.project.api.dtos.response.ProjectMemberPageResponse;
import com.dev.tasktrackr.project.service.ProjectMemberService;
import com.dev.tasktrackr.shared.api.annotation.ApiErrorResponses;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/projects")
@RequiredArgsConstructor
@Slf4j
@ApiErrorResponses.SecuredResourceEndpoint
public class ProjectMemberController {
    private final ProjectMemberService projectMemberService;

    @GetMapping("/{projectId}/members")
    @Operation(
            summary = "Get all members of a project",
            description = "Retrieves a paginated list of all members belonging to the specified project"
    )
    @ApiResponse(
            responseCode = "200",
            description = "Members retrieved successfully",
            content = @Content(schema = @Schema(implementation = ProjectMemberPageResponse.class))
    )
    public ResponseEntity<PageResponse<ProjectMemberDto>> findAllProjectMembersByProjectId(@AuthenticationPrincipal Jwt jwt,
                                                                                           @PathVariable(name = "projectId") Long projectId,
                                                                                           @RequestParam(name = "size", defaultValue = "5") int size,
                                                                                           @RequestParam(name = "page", defaultValue = "0") int page) {
        log.info("User {} requests members in project {}", jwt.getClaimAsString("preferred_username"), projectId);

        PageRequest pr = PageRequest.of(page, size);
        String userId = jwt.getClaim("sub");

        Page<ProjectMemberDto> response = projectMemberService.getAllProjectMembers(userId, projectId, pr);
        return ResponseEntity.ok(PageResponse.from(response));
    }

    @DeleteMapping("/{projectId}/members/{memberId}")
    @Operation(
            summary = "Remove a member from a project",
            description = "Removes a specific member by their ID from the given project"
    )
    @ApiResponse(responseCode = "204", description = "Member removed successfully")
    public ResponseEntity<Void> removeMember(@AuthenticationPrincipal Jwt jwt,
                                             @PathVariable(name = "projectId") Long projectId,
                                             @PathVariable(name = "memberId") Long memberId) {
        log.info("User {} requests to remove Member {} from project {}", jwt.getClaimAsString("preferred_username"), memberId, projectId);

        String userId = jwt.getClaim("sub");

        projectMemberService.removeMemberFromProject(userId, projectId, memberId);

        return ResponseEntity.noContent().build();
    }

}
