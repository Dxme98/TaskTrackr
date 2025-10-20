package com.dev.tasktrackr.project.api.dtos.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ScrumMemberStatisticDto {

    @Schema(
            description = "Benutzername des Scrum-Mitglieds.",
            example = "julia.schmidt"
    )
    String username;

    @Schema(
            description = "Gesamtzahl der Tasks, die diesem Mitglied im Sprint zugewiesen sind.",
            example = "10"
    )
    int totalTasks;

    @Schema(
            description = "Anzahl der von diesem Mitglied abgeschlossenen Tasks (Status 'Done').",
            example = "5"
    )
    int finishedTasks;

    @Schema(
            description = "Prozentsatz der abgeschlossenen Tasks (ganzzahlig, 0-100).",
            example = "50"
    )
    int finishedTasksPercentage;

    @Schema(
            description = "Gesamtzahl der Story Points, die diesem Mitglied zugewiesen sind.",
            example = "20"
    )
    int totalPoints;

    @Schema(
            description = "Anzahl der Story Points, die von diesem Mitglied abgeschlossen wurden.",
            example = "13"
    )
    int finishedPoints;

    @Schema(
            description = "Anzahl der Tasks dieses Mitglieds, die aktuell als 'Blocker' markiert sind.",
            example = "1"
    )
    int totalBlocker;

    @Schema(
            description = "Anzahl der Tasks dieses Mitglieds im Status 'Backlog'",
            example = "2"
    )
    int tasksInBacklog;

    @Schema(
            description = "Anzahl der Tasks dieses Mitglieds im Status 'In Progress'.",
            example = "2"
    )
    int tasksInProgress;

    @Schema(
            description = "Anzahl der Tasks dieses Mitglieds im Status 'Review'.",
            example = "1"
    )
    int tasksInReview;

    @Schema(
            description = "Anzahl der Tasks dieses Mitglieds im Status 'Done'.",
            example = "5"
    )
    int tasksInDone;
}
