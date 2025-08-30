package com.dev.tasktrackr.project.api;

import com.dev.tasktrackr.project.api.dtos.request.ProjectInviteRequest;
import com.dev.tasktrackr.project.api.dtos.response.ProjectInviteResponseDto;
import com.dev.tasktrackr.project.api.dtos.response.ProjectOverviewDto;
import com.dev.tasktrackr.project.service.ProjectInviteService;
import com.dev.tasktrackr.shared.api.annotation.ApiErrorResponses;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/invites")
@RequiredArgsConstructor
@Slf4j
@ApiErrorResponses.SecuredResourceEndpoint
public class ProjectInviteController {
    private final ProjectInviteService projectInviteService;

    @PostMapping
    @Operation(summary = "Create a new Invite", description = "Creates a new Projectinvite")
    @ApiResponse(responseCode = "201", description = "Invite created successfully",
            content = @Content(schema = @Schema(implementation = ProjectInviteResponseDto.class)))
    @ApiErrorResponses.Conflict
    public ResponseEntity<ProjectInviteResponseDto> createInvite(@AuthenticationPrincipal Jwt jwt, @Valid @RequestBody ProjectInviteRequest inviteRequest) {
        log.info("Creating Invite from user: {}", jwt.getClaimAsString("preferred_username"));

        String userId = jwt.getClaim("sub");
        ProjectInviteResponseDto response = projectInviteService.createProjectInvite(inviteRequest, userId);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/{inviteId}/accept")
    @Operation(summary = "Accept a Invite", description = "Sets Invite Status to ACCEPTED and adds Receiver to Project")
    @ApiResponse(responseCode = "200", description = "Invite accepted successfully",
            content = @Content(schema = @Schema(implementation = ProjectInviteResponseDto.class)))
    @ApiErrorResponses.Conflict
    public ResponseEntity<ProjectInviteResponseDto> acceptInvite(@AuthenticationPrincipal Jwt jwt, @PathVariable Long inviteId) {
        log.info("Accepting Invite from user: {}", jwt.getClaimAsString("preferred_username"));

        String userId = jwt.getClaim("sub");
        ProjectInviteResponseDto response = projectInviteService.acceptProjectInvite(userId, inviteId);

        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @PutMapping("/{inviteId}/decline")
    @Operation(summary = "Decline a Invite", description = "Sets InviteStatus to Declined")
    @ApiResponse(responseCode = "200", description = "Invite declined successfully",
            content = @Content(schema = @Schema(implementation = ProjectInviteResponseDto.class)))
    @ApiErrorResponses.Conflict
    public ResponseEntity<ProjectInviteResponseDto> declineInvite(@AuthenticationPrincipal Jwt jwt, @PathVariable Long inviteId) {
        log.info("Declined Invite from user: {}", jwt.getClaimAsString("preferred_username"));

        String userId = jwt.getClaim("sub");
        ProjectInviteResponseDto response = projectInviteService.declineProjectInvite(userId, inviteId);

        return ResponseEntity.status(HttpStatus.OK).body(response);
    }
}
