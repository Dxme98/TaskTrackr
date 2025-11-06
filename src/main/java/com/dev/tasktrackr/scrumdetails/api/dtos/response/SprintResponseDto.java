package com.dev.tasktrackr.scrumdetails.api.dtos.response;

import com.dev.tasktrackr.scrumdetails.domain.SprintStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class SprintResponseDto {

    @Schema(
            description = "Die eindeutige ID des Sprints.",
            example = "12"
    )
    private Long id;

    @Schema(
            description = "Der Name des Sprints.",
            example = "Sprint Bravo - Q4/2024"
    )
    private String name;

    @Schema(
            description = "Das definierte Ziel dieses Sprints.",
            example = "Release der Beta-Version für interne Tester."
    )
    private String goal;

    @Schema(
            description = "Eine detaillierte Beschreibung der Sprint-Inhalte.",
            example = "Fokussierung auf die Behebung von Prio-1-Bugs und Fertigstellung des Reportings."
    )
    private String description;

    @Schema(
            description = "Der aktuelle Status des Sprints.",
            example = "ACTIVE",
            allowableValues = {  "PLANNED", "ACTIVE", "DONE"}
    )
    private SprintStatus status;

    @Schema(
            description = "Startdatum des Sprints (Format: YYYY-MM-DD).",
            example = "2024-11-05"
    )
    private LocalDate startDate;

    @Schema(
            description = "Geplantes Enddatum des Sprints (Format: YYYY-MM-DD).",
            example = "2024-11-19"
    )
    private LocalDate endDate;

    @Schema(
            description = "Gesamtzahl der Stories, die für diesen Sprint geplant wurden.",
            example = "20"
    )
    private int totalStories;

    @Schema(
            description = "Anzahl der Stories, die in diesem Sprint bereits abgeschlossen ('Done') sind.",
            example = "10"
    )
    private int completedStories;

    @Schema(
            description = "Gesamtzahl der Story Points, die für diesen Sprint geplant wurden.",
            example = "100"
    )
    private int totalStoryPoints;

    @Schema(
            description = "Anzahl der Story Points, die in diesem Sprint bereits abgeschlossen ('Done') sind.",
            example = "45"
    )
    private int completedStoryPoints;

    @Schema(
            description = "Fortschritt des Sprints in Prozent (basierend auf Story Points).",
            example = "45.0"
    )
    private double progressPercentage;

    @Schema(
            description = "Eine Zusammenfassung der Backlog-Items, die diesem Sprint zugeordnet sind."
    )
    private Set<SprintSummaryItemResponse> sprintSummaryItems = new HashSet<>();
}
