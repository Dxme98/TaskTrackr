package com.dev.tasktrackr.project.api.controller;

import com.dev.tasktrackr.project.api.dtos.request.CreateLinkRequest;
import com.dev.tasktrackr.project.api.dtos.request.UpdateInformationContentRequest;
import com.dev.tasktrackr.project.domain.basic.Information;
import com.dev.tasktrackr.project.domain.basic.Link;
import com.dev.tasktrackr.project.service.ProjectInformationService;
import com.dev.tasktrackr.shared.api.annotation.ApiErrorResponses;
import io.swagger.v3.oas.annotations.Operation;
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

import java.util.Set;

@RestController
@RequestMapping("/api/v1/projects/{projectId}")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Information", description = "Endpoints for managing Information and Links in the Project")
@ApiErrorResponses.SecuredResourceEndpoint
public class ProjectInformationController {

    private final ProjectInformationService projectInformationService;

    @GetMapping("/information")
    @Operation(
            summary = "Get project information content",
            description = "Retrieves the overview information content for the specified project."
    )
    @ApiResponse(responseCode = "200", description = "Information content retrieved successfully.")
    public ResponseEntity<Information> findContentByProjectId(@AuthenticationPrincipal Jwt jwt,
                                                              @PathVariable (name="projectId") Long projectId) {
        String userId = jwt.getClaim("sub");
        log.info("User {} requests overview information for project {}", jwt.getClaimAsString("preferred_username"), projectId);

        Information information = projectInformationService.findContentByProjectId(projectId, userId);
        return ResponseEntity.ok(information);
    }

    @PutMapping("/information")
    @Operation(
            summary = "Update project information content",
            description = "Updates the overview information content for the specified project."
    )
    @ApiResponse(responseCode = "200", description = "Information content updated successfully.")
    public ResponseEntity<Information> updateContent(@AuthenticationPrincipal Jwt jwt,
                                                     @PathVariable (name="projectId") Long projectId,
                                                     @RequestBody @Valid UpdateInformationContentRequest request) {
        String userId = jwt.getClaim("sub");
        log.info("User {} requests to update overview information for project {}", jwt.getClaimAsString("preferred_username"), projectId);

        Information updatedInformation = projectInformationService.updateContent(projectId, userId, request);
        return ResponseEntity.ok(updatedInformation);
    }

    @GetMapping("/links")
    @Operation(
            summary = "Get all generated links of the specified project",
            description = "Retrieves all links associated with the overview of the specified project."
    )
    @ApiResponse(responseCode = "200", description = "Links retrieved successfully.")
    public ResponseEntity<Set<Link>> findLinksByProjectId(@AuthenticationPrincipal Jwt jwt,
                                                          @PathVariable (name="projectId") Long projectId) {
        String userId = jwt.getClaim("sub");
        log.info("User {} requests overview links for project {}", jwt.getClaimAsString("preferred_username"), projectId);

        Set<Link> links = projectInformationService.findLinksByProjectId(projectId, userId);
        return ResponseEntity.ok(links);
    }

    @PostMapping("/links")
    @Operation(
            summary = "Add a link to the project overview",
            description = "Adds a new link to the overview of the specified project."
    )
    @ApiResponse(responseCode = "201", description = "Link added successfully.")
    public ResponseEntity<Link> addLink(@AuthenticationPrincipal Jwt jwt,
                                        @PathVariable (name="projectId") Long projectId,
                                        @RequestBody @Valid CreateLinkRequest request) {
        String userId = jwt.getClaim("sub");
        log.info("User {} requests to add a link to the overview of project {}", jwt.getClaimAsString("preferred_username"), projectId);

        Link addedLink = projectInformationService.addLink(projectId, userId, request);
        return new ResponseEntity<>(addedLink, HttpStatus.CREATED);
    }

    @DeleteMapping("/links/{linkId}")
    @Operation(
            summary = "Delete a link from the project overview",
            description = "Deletes a specific link from the overview of the specified project."
    )
    @ApiResponse(responseCode = "204", description = "Link deleted successfully.")
    public ResponseEntity<Void> deleteLink(@AuthenticationPrincipal Jwt jwt,
                                           @PathVariable (name="projectId") Long projectId,
                                           @PathVariable (name="linkId") Long linkId) {
        String userId = jwt.getClaim("sub");
        log.info("User {} requests to delete link {} from the overview of project {}", jwt.getClaimAsString("preferred_username"), linkId, projectId);

        projectInformationService.deleteLink(projectId, userId, linkId);
        return ResponseEntity.noContent().build();
    }
}