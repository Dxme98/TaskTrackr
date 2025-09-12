package com.dev.tasktrackr.project.api.controller;

import com.dev.tasktrackr.project.api.dtos.request.ProjectInviteRequest;
import com.dev.tasktrackr.project.api.dtos.response.PageResponse;
import com.dev.tasktrackr.project.api.dtos.response.ProjectInvitePageResponse;
import com.dev.tasktrackr.project.api.dtos.response.ProjectInviteResponseDto;
import com.dev.tasktrackr.project.service.ProjectInviteService;
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
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
@Slf4j
@ApiErrorResponses.SecuredResourceEndpoint
public class ProjectInviteController {
    private final ProjectInviteService projectInviteService;

    @PostMapping("/projects/{projectId}/invites")
    @Operation(summary = "Create a new Invite", description = "Creates a new Projectinvite")
    @ApiResponse(responseCode = "201", description = "Invite created successfully",
            content = @Content(schema = @Schema(implementation = ProjectInviteResponseDto.class)))
    @ApiErrorResponses.Conflict
    public ResponseEntity<ProjectInviteResponseDto> createInvite(@AuthenticationPrincipal Jwt jwt, @Valid @RequestBody ProjectInviteRequest inviteRequest,
                                                                 @PathVariable(name = "projectId") Long projectId) {
        log.info("Creating Invite from user: {}", jwt.getClaimAsString("preferred_username"));

        String userId = jwt.getClaim("sub");
        ProjectInviteResponseDto response = projectInviteService.createProjectInvite(inviteRequest, userId, projectId);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/invites/{inviteId}/accept")
    @Operation(summary = "Accept a Invite", description = "Sets Invite Status to ACCEPTED and adds Receiver to Project")
    @ApiResponse(responseCode = "200", description = "Invite accepted successfully",
            content = @Content(schema = @Schema(implementation = ProjectInviteResponseDto.class)))
    @ApiErrorResponses.Conflict
    public ResponseEntity<ProjectInviteResponseDto> acceptInvite(@AuthenticationPrincipal Jwt jwt, @PathVariable(name = "inviteId") Long inviteId) {
        log.info("Accepting Invite from user: {}", jwt.getClaimAsString("preferred_username"));

        String userId = jwt.getClaim("sub");
        ProjectInviteResponseDto response = projectInviteService.acceptProjectInvite(userId, inviteId);

        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @PutMapping("/invites/{inviteId}/decline")
    @Operation(summary = "Decline a Invite", description = "Sets InviteStatus to Declined")
    @ApiResponse(responseCode = "200", description = "Invite declined successfully",
            content = @Content(schema = @Schema(implementation = ProjectInviteResponseDto.class)))
    @ApiErrorResponses.Conflict
    public ResponseEntity<ProjectInviteResponseDto> declineInvite(@AuthenticationPrincipal Jwt jwt, @PathVariable(name = "inviteId") Long inviteId) {
        log.info("Declined Invite from user: {}", jwt.getClaimAsString("preferred_username"));

        String userId = jwt.getClaim("sub");
        ProjectInviteResponseDto response = projectInviteService.declineProjectInvite(userId, inviteId);

        return ResponseEntity.status(HttpStatus.OK).body(response);
    }


    @GetMapping("/invites")
    @Operation(
            summary = "Get all pending project invites for the current user",
            description = "Returns a paginated list of all project invites with status PENDING where the authenticated user is the receiver. " +
                    "Results are sorted by creation date in descending order (newest first)."
    )
    @ApiResponse(
            responseCode = "200",
            description = "Pending invites loaded successfully",
            content = @Content(schema = @Schema(implementation = ProjectInvitePageResponse.class))
    )
    public ResponseEntity<PageResponse<ProjectInviteResponseDto>> findAllPendingInvitesByUserId(@AuthenticationPrincipal Jwt jwt,
                                                                                @RequestParam(defaultValue = "0", name = "page") int page ,
                                                                                @RequestParam(defaultValue = "5", name = "size") int size) {
        log.info("Received request to list all PENDING invites from: : {}", jwt.getClaimAsString("preferred_username"));

        String userId = jwt.getClaim("sub");
        PageRequest pageRequest = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<ProjectInviteResponseDto> response = projectInviteService.findAllPendingInvitesByUserId(userId, pageRequest);

        return ResponseEntity.status(HttpStatus.OK).body(PageResponse.from(response));
    }
}
