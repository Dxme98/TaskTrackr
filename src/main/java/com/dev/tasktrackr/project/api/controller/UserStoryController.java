package com.dev.tasktrackr.project.api.controller;

import com.dev.tasktrackr.project.api.dtos.request.CreateUserStoryRequest;
import com.dev.tasktrackr.project.api.dtos.response.UserStoryPageResponse;
import com.dev.tasktrackr.project.api.dtos.response.UserStoryResponseDto;
import com.dev.tasktrackr.project.domain.scrum.StoryStatus;
import com.dev.tasktrackr.project.service.UserStoryService;
import com.dev.tasktrackr.shared.api.annotation.ApiErrorResponses; // Angenommen, diese Annotation existiert

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import com.dev.tasktrackr.project.api.dtos.response.PageResponse;


@RestController
@RequestMapping("/api/v1/projects/{projectId}/userStories")
@Tag(name = "User Stories", description = "Operationen zur Verwaltung von User Stories innerhalb eines Scrum-Projekts")
@Slf4j
@RequiredArgsConstructor
@ApiErrorResponses.SecuredResourceEndpoint
public class UserStoryController {
    private final UserStoryService userStoryService;

    @PostMapping
    @Operation(
            summary = "Erstellt eine neue User Story",
            description = "Erstellt eine neue User Story innerhalb des angegebenen Projekts."
    )
    @ApiResponse(
            responseCode = "201",
            description = "User Story erfolgreich erstellt",
            content = @Content(schema = @Schema(implementation = UserStoryResponseDto.class))
    )
    @ApiErrorResponses.BadRequest
    public ResponseEntity<UserStoryResponseDto> createUserStory(
            @PathVariable(name = "projectId") Long projectId,
            @Valid @RequestBody CreateUserStoryRequest createUserStoryRequest,
            @AuthenticationPrincipal Jwt jwt) {

        String jwtUserId = jwt.getClaim("sub");
        log.info("Anfrage zum Erstellen einer User Story im Projekt {} von Benutzer {}", projectId, jwtUserId);

        UserStoryResponseDto response = userStoryService.createUserStory(projectId, createUserStoryRequest, jwtUserId);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @DeleteMapping("/{userStoryId}")
    @Operation(summary = "Löscht eine User Story", description = "Löscht eine User Story anhand ihrer ID aus dem angegebenen Projekt.")
    @ApiResponse(responseCode = "204", description = "User Story erfolgreich gelöscht")
    public ResponseEntity<Void> deleteUserStory(
            @PathVariable(name = "projectId") Long projectId,
            @PathVariable(name = "userStoryId") Long userStoryId,
            @AuthenticationPrincipal Jwt jwt) {

        String jwtUserId = jwt.getClaim("sub");
        log.info("Anfrage zum Löschen der User Story {} im Projekt {} von Benutzer {}", userStoryId, projectId, jwtUserId);

        userStoryService.deleteUserStory(projectId, userStoryId, jwtUserId);

        return ResponseEntity.noContent().build();
    }

    @GetMapping
    @Operation(
            summary = "Listet User Stories für ein Projekt auf",
            description = "Gibt eine paginierte Liste aller User Stories für das angegebene Projekt zurück."
    )
    @ApiResponse(
            responseCode = "200",
            description = "User Stories erfolgreich geladen",
            content = @Content(schema = @Schema(implementation = UserStoryPageResponse.class))
    )
    public ResponseEntity<PageResponse<UserStoryResponseDto>> getUserStories(
            @PathVariable(name = "projectId") Long projectId,
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "20") int size,
            @RequestParam(name = "sortBy", defaultValue = "createdAt") String sortBy,
            @RequestParam(name = "sortDir", defaultValue = "asc") String sortDir,
            @RequestParam(name = "filter", required = false) String filter,
            @AuthenticationPrincipal Jwt jwt) {

        String jwtUserId = jwt.getClaim("sub");
        log.info("Anfrage zum Abrufen der User Stories für Projekt {} von Benutzer {}", projectId, jwtUserId);

        Sort sort = Sort.by(Sort.Direction.fromString(sortDir), sortBy);
        Pageable pageable = PageRequest.of(page, size, sort);
        Page<UserStoryResponseDto> userStoryPage = userStoryService.getUserStoriesByProjectId(projectId, pageable, jwtUserId, filter);

        return ResponseEntity.ok(PageResponse.from(userStoryPage));
    }
}