package com.dev.tasktrackr.project.api.dtos.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class ScrumProjectStatisticsDto {

    @Schema(
            description = "Anzahl der Sprints, die für dieses Projekt bereits abgeschlossen wurden.",
            example = "4"
    )
    private Long finishedSprints = 0L;

    @Schema(
            description = "Gesamtzahl der Story Points, die über alle abgeschlossenen Sprints hinweg fertiggestellt wurden.",
            example = "210"
    )
    private Long totalCompletedPoints = 0L;

    @Schema(
            description = "Durchschnittliche Anzahl an Story Points, die pro Sprint abgeschlossen wurden (Velocity).",
            example = "52"
    )
    private Long averageVelocity = 0L;

    public ScrumProjectStatisticsDto(Long finishedSprints, Long totalCompletedPoints) {
        this.finishedSprints = finishedSprints;
        this.totalCompletedPoints = totalCompletedPoints;
        this.averageVelocity = totalCompletedPoints / finishedSprints;
    }
}
