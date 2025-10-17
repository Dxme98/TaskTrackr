package com.dev.tasktrackr.project.api.controller;

import com.dev.tasktrackr.project.api.dtos.response.ScrumReportsDto;
import com.dev.tasktrackr.project.service.ScrumReportsService;
import com.dev.tasktrackr.shared.api.annotation.ApiErrorResponses;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/projects/{projectId}/scrum-reports")
@RequiredArgsConstructor
@Tag(name = "Scrum Reports", description = "Endpoints for retrieving Scrum project statistics and reports.")
@ApiErrorResponses.SecuredResourceEndpoint
public class ScrumReportsController {

    private final ScrumReportsService scrumReportsService;

    @GetMapping
    @Operation(summary = "Get Scrum project report",
            description = "Retrieves a comprehensive report for a Scrum project, including active sprint data, team member statistics, and overall project velocity.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Report retrieved successfully",
                    content = @Content(schema = @Schema(implementation = ScrumReportsDto.class)))
    })
    public ResponseEntity<ScrumReportsDto> getScrumReport(
            @PathVariable(name = "projectId") Long projectId,
            @AuthenticationPrincipal Jwt jwt) {

        String jwtUserId = jwt.getClaim("sub");
        ScrumReportsDto response = scrumReportsService.getScrumReport(projectId, jwtUserId);
        return ResponseEntity.ok(response);
    }
}
