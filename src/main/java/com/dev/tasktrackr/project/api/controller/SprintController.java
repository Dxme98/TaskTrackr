package com.dev.tasktrackr.project.api.controller;

import com.dev.tasktrackr.project.api.dtos.request.CreateSprintRequest;
import com.dev.tasktrackr.project.api.dtos.response.PageResponse;
import com.dev.tasktrackr.project.api.dtos.response.SprintPageResponse;
import com.dev.tasktrackr.project.api.dtos.response.SprintResponseDto;
import com.dev.tasktrackr.project.domain.scrum.SprintStatus;
import com.dev.tasktrackr.project.service.SprintService;
import com.dev.tasktrackr.shared.api.annotation.ApiErrorResponses;
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
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/projects/{projectId}/sprints")
@Tag(name = "Sprints", description = "Operationen zur Verwaltung von Sprints innerhalb eines Scrum-Projekts")
@Slf4j
@RequiredArgsConstructor
@ApiErrorResponses.CommonErrors
public class SprintController {

    private final SprintService sprintService;

    @PostMapping
    @Operation(summary = "Erstellt einen neuen Sprint", description = "Erstellt einen neuen Sprint innerhalb des angegebenen Projekts und weist ihm User Stories zu.")
    @ApiResponse(responseCode = "201", description = "Sprint erfolgreich erstellt", content = @Content(schema = @Schema(implementation = SprintResponseDto.class)))
    @ApiErrorResponses.BadRequest
    public ResponseEntity<SprintResponseDto> createSprint(
            @PathVariable Long projectId,
            @Valid @RequestBody CreateSprintRequest createSprintRequest,
            @AuthenticationPrincipal Jwt jwt) {

        String jwtUserId = jwt.getClaim("sub");
        log.info("Anfrage zum Erstellen eines Sprints im Projekt {} von Benutzer {}", projectId, jwtUserId);
        SprintResponseDto response = sprintService.createSprint(createSprintRequest, projectId, jwtUserId);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{status}")
    @Operation(summary = "Listet alle Sprints für ein Projekt auf",
            description = "Gibt eine paginierte Liste aller Sprints für das angegebene Projekt zurück.")
    @ApiResponse(responseCode = "200",
            description = "Sprints erfolgreich geladen",
            content = @Content(schema = @Schema(implementation = SprintPageResponse.class)))
    public ResponseEntity<PageResponse<SprintResponseDto>> getAllSprints(
            @PathVariable Long projectId,
            @PathVariable SprintStatus status,
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "20") int size,
            @AuthenticationPrincipal Jwt jwt) {

        String jwtUserId = jwt.getClaim("sub");
        log.info("Anfrage zum Abrufen der Sprints für Projekt {} von Benutzer {}", projectId, jwtUserId);
        PageRequest pageable = PageRequest.of(page, size);
        Page<SprintResponseDto> sprintPage = sprintService.findAllSprintsByProjectIdAndStatus(projectId, jwtUserId, pageable, status);
        return ResponseEntity.ok(PageResponse.from(sprintPage));
    }

    @GetMapping("/active")
    @Operation(summary = "Findet den aktiven Sprint", description = "Gibt den aktuell laufenden Sprint für das angegebene Projekt zurück.")
    @ApiResponse(responseCode = "200", description = "Aktiver Sprint erfolgreich gefunden")
    public ResponseEntity<SprintResponseDto> getActiveSprint(
            @PathVariable Long projectId,
            @AuthenticationPrincipal Jwt jwt) {

        String jwtUserId = jwt.getClaim("sub");
        log.info("Anfrage zum Finden des aktiven Sprints für Projekt {} von Benutzer {}", projectId, jwtUserId);
        SprintResponseDto response = sprintService.findActiveSprint(projectId, jwtUserId);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{sprintId}/start")
    @Operation(summary = "Startet einen geplanten Sprint", description = "Ändert den Status eines Sprints von 'PLANNED' zu 'IN_PROGRESS'.")
    @ApiResponse(responseCode = "200", description = "Sprint erfolgreich gestartet")
    public ResponseEntity<SprintResponseDto> startSprint(
            @PathVariable Long projectId,
            @PathVariable Long sprintId,
            @AuthenticationPrincipal Jwt jwt) {

        String jwtUserId = jwt.getClaim("sub");
        log.info("Anfrage zum Starten von Sprint {} in Projekt {} von Benutzer {}", sprintId, projectId, jwtUserId);
        SprintResponseDto response = sprintService.startSprint(sprintId, projectId, jwtUserId);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{sprintId}/end")
    @Operation(summary = "Beendet einen laufenden Sprint", description = "Ändert den Status eines Sprints von 'ACTIVE' zu 'DONE'.")
    @ApiResponse(responseCode = "200", description = "Sprint erfolgreich beendet")
    public ResponseEntity<SprintResponseDto> endSprint(
            @PathVariable Long projectId,
            @PathVariable Long sprintId,
            @AuthenticationPrincipal Jwt jwt) {

        String jwtUserId = jwt.getClaim("sub");
        log.info("Anfrage zum Beenden von Sprint {} in Projekt {} von Benutzer {}", sprintId, projectId, jwtUserId);
        SprintResponseDto response = sprintService.endSprint(sprintId, projectId, jwtUserId);
        return ResponseEntity.ok(response);
    }
}