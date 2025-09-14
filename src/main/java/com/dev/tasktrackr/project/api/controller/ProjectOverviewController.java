package com.dev.tasktrackr.project.api.controller;

import com.dev.tasktrackr.project.api.dtos.request.UpdateInformationContentRequest;
import com.dev.tasktrackr.project.domain.Information;
import com.dev.tasktrackr.project.service.ProjectOverviewService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/projects/{projectId}")
@RequiredArgsConstructor
public class ProjectOverviewController {
    private final ProjectOverviewService projectOverviewService;

    @PutMapping("/content")
    public ResponseEntity<Information> updateContent(@AuthenticationPrincipal Jwt jwt, @PathVariable Long projectId,
                                                     @RequestBody @Valid UpdateInformationContentRequest updateInformationContentRequest) {
        String userId = jwt.getClaim("sub");

        Information response = projectOverviewService.updateContent(projectId, userId, updateInformationContentRequest);

        return ResponseEntity.ok(response);
    }
}
