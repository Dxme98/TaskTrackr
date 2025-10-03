package com.dev.tasktrackr.activity;

import com.dev.tasktrackr.project.api.dtos.response.PageResponse;
import com.dev.tasktrackr.shared.api.annotation.ApiErrorResponses;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/projects")
@RequiredArgsConstructor
@Tag(name = "Projects", description = "Project management operations")
@ApiErrorResponses.SecuredResourceEndpoint
public class ProjectActivityController {
    private final ProjectActivityService projectActivityService;

    @GetMapping("/{projectId}/activities")
    @Operation(summary = "Get project activities", description = "Gibt eine paginierte Liste von Aktivitäten für ein bestimmtes Projekt zurück.")
    @ApiResponse(responseCode = "200", description = "Aktivitäten erfolgreich geladen",
            content = @Content(schema = @Schema(implementation = ProjectActivityPageResponse.class))) // Verwendung der Wrapper-Klasse
    public ResponseEntity<PageResponse<ProjectActivityDto>> getProjectActivities(
            @PathVariable(name = "projectId") Long projectId,
            @AuthenticationPrincipal Jwt jwt,
            @RequestParam(defaultValue = "10", name = "size") int size,
            @RequestParam(defaultValue = "0", name = "page") int page,
            @RequestParam(defaultValue = "createdAt", name = "sortBy") String sortBy,
            @RequestParam(defaultValue = "desc", name = "sortDir") String sortDir) {

        Sort sort = Sort.by(Sort.Direction.fromString(sortDir), sortBy);
        PageRequest pr = PageRequest.of(page, size, sort);
        String userId = jwt.getClaim("sub");

        Page<ProjectActivityDto> response = projectActivityService.findActivitiesByProjectId(userId, projectId, pr);

        return ResponseEntity.ok(PageResponse.from(response));
    }
}
