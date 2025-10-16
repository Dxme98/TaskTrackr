package com.dev.tasktrackr.project.api.controller;

import com.dev.tasktrackr.project.api.dtos.request.AssignMemberRequest;
import com.dev.tasktrackr.project.api.dtos.request.CreateCommentRequest;
import com.dev.tasktrackr.project.api.dtos.request.UpdateStatusRequest;
import com.dev.tasktrackr.project.api.dtos.response.ScrumBoardResponseDto;
import com.dev.tasktrackr.project.api.dtos.response.SprintBacklogItemResponse;
import com.dev.tasktrackr.project.service.ScrumBoardService;
import com.dev.tasktrackr.shared.api.annotation.ApiErrorResponses;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/projects/{projectId}/scrumboard")
@Tag(name = "Scrum Board", description = "Operations related to the project's Scrum Board")
@Slf4j
@RequiredArgsConstructor
@ApiErrorResponses.CommonErrors
public class ScrumBoardController {

    private final ScrumBoardService scrumBoardService;

    @GetMapping
    @Operation(summary = "Get the active Scrum Board", description = "Returns all data needed to display the Scrum Board for the currently active sprint.")
    @ApiResponse(responseCode = "200", description = "Scrum Board loaded successfully",
            content = @Content(schema = @Schema(implementation = ScrumBoardResponseDto.class)))
    public ResponseEntity<ScrumBoardResponseDto> getActiveScrumBoard(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable Long projectId) {
        log.info("Request to get active scrum board for project {}", projectId);
        String userId = jwt.getClaim("sub");
        ScrumBoardResponseDto response = scrumBoardService.getScrumBoard(projectId, userId);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/items/{backlogItemId}/status")
    @Operation(summary = "Update the status of a backlog item", description = "Moves a user story to a different column on the board (e.g., from 'IN_PROGRESS' to 'REVIEW').")
    @ApiResponse(responseCode = "200", description = "Status updated successfully",
            content = @Content(schema = @Schema(implementation = SprintBacklogItemResponse.class)))
    public ResponseEntity<SprintBacklogItemResponse> updateUserStoryStatus(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable Long projectId,
            @PathVariable Long backlogItemId,
            @Valid @RequestBody UpdateStatusRequest request) {
        log.info("Request to update status for backlog item {} in project {}", backlogItemId, projectId);
        String userId = jwt.getClaim("sub");
        SprintBacklogItemResponse response = scrumBoardService.updateUserStoryStatus(projectId, backlogItemId, request.getNewStatus(), userId);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/items/{backlogItemId}/assignees")
    @Operation(summary = "Assign a member to a backlog item")
    @ApiResponse(responseCode = "200", description = "Member assigned successfully",
            content = @Content(schema = @Schema(implementation = SprintBacklogItemResponse.class)))
    public ResponseEntity<SprintBacklogItemResponse> assignMember(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable Long projectId,
            @PathVariable Long backlogItemId,
            @Valid @RequestBody AssignMemberRequest request) {
        log.info("Request to assign member {} to item {}", request.getMemberId(), backlogItemId);
        String userId = jwt.getClaim("sub");
        SprintBacklogItemResponse response = scrumBoardService.assignMemberToStory(projectId, backlogItemId, request.getMemberId(), userId);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/items/{backlogItemId}/assignees/{memberId}")
    @Operation(summary = "Unassign a member from a backlog item")
    @ApiResponse(responseCode = "200", description = "Member unassigned successfully",
            content = @Content(schema = @Schema(implementation = SprintBacklogItemResponse.class)))
    public ResponseEntity<SprintBacklogItemResponse> unassignMember(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable Long projectId,
            @PathVariable Long backlogItemId,
            @PathVariable Long memberId) {
        log.info("Request to unassign member {} from item {}", memberId, backlogItemId);
        String userId = jwt.getClaim("sub");
        SprintBacklogItemResponse response = scrumBoardService.unassignMemberFromStory(projectId, backlogItemId, memberId, userId);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/items/{backlogItemId}/comments")
    @Operation(summary = "Add a comment to a backlog item")
    @ApiResponse(responseCode = "201", description = "Comment added successfully",
            content = @Content(schema = @Schema(implementation = SprintBacklogItemResponse.class)))
    public ResponseEntity<SprintBacklogItemResponse> addComment(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable Long projectId,
            @PathVariable Long backlogItemId,
            @Valid @RequestBody CreateCommentRequest request) {
        log.info("Request to add comment to item {}", backlogItemId);
        String userId = jwt.getClaim("sub");
        SprintBacklogItemResponse response = scrumBoardService.addCommentToStory(projectId, backlogItemId, request, userId);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @DeleteMapping("/items/{backlogItemId}/comments/{commentId}")
    @Operation(summary = "Remove a comment from a backlog item")
    @ApiResponse(responseCode = "204", description = "Comment removed successfully")
    public ResponseEntity<Void> removeComment(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable Long projectId,
            @PathVariable Long backlogItemId,
            @PathVariable Long commentId) {
        log.info("Request to remove comment {} from item {}", commentId, backlogItemId);
        String userId = jwt.getClaim("sub");
        scrumBoardService.removeCommentFromStory(projectId, backlogItemId, commentId, userId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/items/{backlogItemId}/blockers")
    @Operation(summary = "Add a blocker to a backlog item")
    @ApiResponse(responseCode = "201", description = "Blocker added successfully",
            content = @Content(schema = @Schema(implementation = SprintBacklogItemResponse.class)))
    public ResponseEntity<SprintBacklogItemResponse> addBlocker(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable Long projectId,
            @PathVariable Long backlogItemId,
            @Valid @RequestBody CreateCommentRequest request) {
        log.info("Request to add blocker to item {}", backlogItemId);
        String userId = jwt.getClaim("sub");
        SprintBacklogItemResponse response = scrumBoardService.addBlockerToStory(projectId, backlogItemId, request, userId);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @DeleteMapping("/items/{backlogItemId}/blockers/{blockerId}")
    @Operation(summary = "Remove a blocker from a backlog item")
    @ApiResponse(responseCode = "204", description = "Blocker removed successfully")
    public ResponseEntity<Void> removeBlocker(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable Long projectId,
            @PathVariable Long backlogItemId,
            @PathVariable Long blockerId) {
        log.info("Request to remove blocker {} from item {}", blockerId, backlogItemId);
        String userId = jwt.getClaim("sub");
        scrumBoardService.removeBlockerFromStory(projectId, backlogItemId, blockerId, userId);
        return ResponseEntity.noContent().build();
    }
}
