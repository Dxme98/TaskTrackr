package com.dev.tasktrackr.project.api.dtos.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ScrumBoardResponseDto {

    @Schema(
            description = "Der Name des aktuellen Sprints.",
            example = "Sprint Alpha - Q4/2024"
    )
    private String sprintName;

    @Schema(
            description = "Das definierte Ziel des Sprints.",
            example = "Implementierung der initialen Checkout-Funktion."
    )
    private String sprintGoal;

    @Schema(
            description = "Eine detailliertere Beschreibung des Sprints.",
            example = "Fokus auf die Kernfunktionalität des Warenkorbs und Anbindung an den Payment-Provider."
    )
    private String sprintDescription;

    @Schema(
            description = "Startdatum des Sprints (Format: YYYY-MM-DD).",
            example = "2024-10-21"
    )
    private LocalDate startDate;

    @Schema(
            description = "Enddatum des Sprints (Format: YYYY-MM-DD).",
            example = "2024-11-04"
    )
    private LocalDate endDate;

    @Schema(
            description = "Die Summe aller Story Points, die für diesen Sprint geplant wurden.",
            example = "80"
    )
    private int totalStoryPoints;

    @Schema(
            description = "Die Summe der Story Points von 'Done'-Items in diesem Sprint.",
            example = "35"
    )
    private int completedStoryPoints;

    @Schema(
            description = "Liste der Projektmitglieder, die diesem Project zugeordnet sind."
    )
    private List<ProjectMemberDto> projectMembers = new ArrayList<>();

    @Schema(
            description = "Liste der Backlog-Items im Status 'TO DO'."
    )
    private List<SprintBacklogItemResponse> todo = new ArrayList<>();

    @Schema(
            description = "Liste der Backlog-Items im Status 'IN PROGRESS'."
    )
    private List<SprintBacklogItemResponse> inProgress = new ArrayList<>();

    @Schema(
            description = "Liste der Backlog-Items im Status 'REVIEW'."
    )
    private List<SprintBacklogItemResponse> review = new ArrayList<>();

    @Schema(
            description = "Liste der Backlog-Items im Status 'DONE'."
    )
    private List<SprintBacklogItemResponse> done = new ArrayList<>();
}
