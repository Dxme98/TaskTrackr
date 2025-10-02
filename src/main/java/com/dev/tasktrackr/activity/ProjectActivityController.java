package com.dev.tasktrackr.activity;

import com.dev.tasktrackr.project.api.dtos.response.PageResponse;
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
public class ProjectActivityController {
    private final ProjectActivityService projectActivityService;

    @GetMapping("/{projectId}/activities")
    public ResponseEntity<PageResponse<ProjectActivityDto>> getProjectActivities(@PathVariable(name = "projectId") Long projectId, @AuthenticationPrincipal Jwt jwt,
                                                                                 @RequestParam(defaultValue = "10", name = "size") int size,
                                                                                 @RequestParam(defaultValue = "0", name = "page") int page,
                                                                                 @RequestParam(name = "sortBy", defaultValue = "createdAt") String sortBy,
                                                                                 @RequestParam(name = "sortDir", defaultValue = "asc") String sortDir) {
        Sort sort = Sort.by(Sort.Direction.fromString(sortDir), sortBy);
        PageRequest pr = PageRequest.of(page, size, sort);
        String userId = jwt.getClaim("sub");

        Page<ProjectActivityDto> response = projectActivityService.findActivitiesByProjectId(userId, projectId, pr);

        return ResponseEntity.ok(PageResponse.from(response));
    }
}
