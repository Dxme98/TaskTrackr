package com.dev.tasktrackr.project.api.dtos.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ActiveSprintData {

    @Schema(
            description = "Gesamtzahl der Stories im aktiven Sprint.",
            example = "15"
    )
    private int totalStories;

    @Schema(
            description = "Anzahl der bereits abgeschlossenen Stories im Sprint.",
            example = "8"
    )
    private int finishedStories;

    @Schema(
            description = "Gesamtzahl der Story Points (Aufwand) im aktiven Sprint.",
            example = "50"
    )
    private int totalPoints;

    @Schema(
            description = "Anzahl der bereits abgeschlossenen Story Points.",
            example = "25"
    )
    private int finishedPoints;

    @Schema(
            description = "Verbleibende Tage im aktiven Sprint.",
            example = "3"
    )
    private int daysLeft;

    @Schema(
            description = "Durchschnittliche Velocity (abgeschlossene Points pro Tag) in diesem Sprint.",
            example = "7"
    )
    private int averageDailyVelocity;
}
