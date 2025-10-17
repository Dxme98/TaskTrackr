package com.dev.tasktrackr.project.api.dtos.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ScrumProjectStatisticsDto {
    private int finishedSprints;
    private int totalCompletedPoints;
    private int averageVelocity; // Durchschnittliche abgeschlossene punkte pro sprint
}
