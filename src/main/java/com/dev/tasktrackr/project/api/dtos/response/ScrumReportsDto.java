package com.dev.tasktrackr.project.api.dtos.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class ScrumReportsDto {

    @Schema(
            description = "Daten und Metriken zum aktuell laufenden Sprint."
    )
    private ActiveSprintData activeSprintData;

    @Schema(
            description = "Eine Liste von detaillierten Statistiken für jedes einzelne Scrum-Mitglied."
    )
    private List<ScrumMemberStatisticDto> scrumMemberStatisticDtos;

    @Schema(
            description = "Übergeordnete Statistiken für das gesamte Scrum-Projekt (z.B. Velocity)."
    )
    private ScrumProjectStatisticsDto scrumProjectStatisticsDto;
}
