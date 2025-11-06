package com.dev.tasktrackr.scrumdetails.api.dtos.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
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
    Long totalTasks;

    @Schema(
            description = "Gesamtzahl der Story Points, die diesem Mitglied zugewiesen sind.",
            example = "20"
    )
    Long totalPoints;

    @Schema(
            description = "Anzahl der Story Points, die von diesem Mitglied abgeschlossen wurden.",
            example = "13"
    )
    Long finishedPoints;

    @Schema(
            description = "Anzahl der Tasks dieses Mitglieds, die aktuell als 'Blocker' markiert sind.",
            example = "1"
    )
    Long totalBlocker;

    @Schema(
            description = "Anzahl der Tasks dieses Mitglieds im Status 'Backlog'",
            example = "2"
    )
    Long tasksInBacklog;

    @Schema(
            description = "Anzahl der Tasks dieses Mitglieds im Status 'In Progress'.",
            example = "2"
    )
    Long tasksInProgress;

    @Schema(
            description = "Anzahl der Tasks dieses Mitglieds im Status 'Review'.",
            example = "1"
    )
    Long tasksInReview;

    @Schema(
            description = "Anzahl der Tasks dieses Mitglieds im Status 'Done'.",
            example = "5"
    )
    Long tasksInDone;

    @Schema(
            description = "Prozentsatz der abgeschlossenen Tasks (ganzzahlig, 0-100).",
            example = "50"
    )
    int doneTasksPercentage;


    public ScrumMemberStatisticDto(
            String username,
            Long totalTasksL,
            Long totalPointsL,
            Long finishedPointsL,
            Long totalBlockerL,
            Long tasksInBacklogL,
            Long tasksInProgressL,
            Long tasksInReviewL,
            Long tasksInDoneL
    ) {
        this.username = username;
        this.totalTasks = totalTasksL;
        this.totalPoints = totalPointsL;
        this.finishedPoints = finishedPointsL;
        this.totalBlocker = totalBlockerL;
        this.tasksInBacklog = tasksInBacklogL;
        this.tasksInProgress = tasksInProgressL;
        this.tasksInReview = tasksInReviewL;
        this.tasksInDone = tasksInDoneL;

        if (this.totalTasks == 0) {
            this.doneTasksPercentage = 0;
        } else {
            this.doneTasksPercentage = (int) Math.round(
                    ((double) this.tasksInDone / this.totalTasks) * 100
            );
        }
    }
}