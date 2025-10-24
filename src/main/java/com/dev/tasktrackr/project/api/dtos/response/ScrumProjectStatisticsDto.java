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
    private Long finishedSprints;

    @Schema(
            description = "Gesamtzahl der Story Points, die über alle abgeschlossenen Sprints hinweg fertiggestellt wurden.",
            example = "210"
    )
    private Long totalCompletedPoints;

    @Schema(
            description = "Durchschnittliche Anzahl an Story Points, die pro Sprint abgeschlossen wurden (Velocity).",
            example = "52"
    )
    private Long averageVelocity;

    public ScrumProjectStatisticsDto(Long finishedSprints, Long totalCompletedPoints) {
        this.finishedSprints = finishedSprints;
        this.totalCompletedPoints = totalCompletedPoints;
        this.averageVelocity = totalCompletedPoints / finishedSprints;
    }
}
