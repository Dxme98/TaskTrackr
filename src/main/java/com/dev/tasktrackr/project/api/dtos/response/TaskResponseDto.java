package com.dev.tasktrackr.project.api.dtos.response;

import com.dev.tasktrackr.project.domain.enums.Priority;
import com.dev.tasktrackr.project.domain.enums.Status;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Schema(name = "TaskResponseDto")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class TaskResponseDto {

    @Schema(description = "ID der Aufgabe", example = "42")
    private Long id;

    @Schema(description = "ID des Projekts, zu dem die Aufgabe gehört", example = "7")
    private Long projectId;

    @Schema(description = "Titel der Aufgabe", example = "Implementiere Login-Feature")
    private String title;

    @Schema(description = "Beschreibung der Aufgabe", example = "Die Aufgabe umfasst die Implementierung des Login-Workflows inkl. JWT.")
    private String description;

    @Schema(description = "Priorität der Aufgabe", example = "HIGH")
    private Priority priority;

    @Schema(description = "Status der Aufgabe: [IN_PROGRESS, EXPIRED, COMPLETED]", example = "IN_PROGRESS")
    private Status status;

    @Schema(description = "Fälligkeitsdatum", example = "2025-12-31T23:59:59")
    private LocalDateTime dueDate;

    @Schema(description = "Erstellungszeitpunkt", example = "2025-09-14T10:15:30")
    private LocalDateTime createdAt;

    @Schema(description = "Letzte Aktualisierung", example = "2025-09-14T11:00:00")
    private LocalDateTime updatedAt;

    @Schema(description = "Ersteller der Aufgabe")
    private ProjectMemberOverviewDto createdBy;

    @Schema(description = "Letzter Bearbeiter der Aufgabe")
    private ProjectMemberOverviewDto updatedBy;

    @Schema(description = "Usernames und IDs der zugewiesenen Projektmitglieder")
    private Set<ProjectMemberOverviewDto> assignedToMembers = new HashSet<>();
}
