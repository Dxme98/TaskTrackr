package com.dev.tasktrackr.project.api.dtos.response;

import com.dev.tasktrackr.project.domain.enums.ProjectType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
@Builder
@Schema(name = "ProjectOverviewResponse", description = "Übersichtsinformationen eines Projekts")
public class ProjectOverviewDto {

    @Schema(description = "ID des Projekts", example = "456")
    private Long id;

    @Schema(description = "Name des Projekts", example = "Mein Projektname")
    private String name;

    @Schema(
            description = "Zeitpunkt der Projekterstellung im ISO-8601 Format",
            example = "2024-01-15T14:30:00"
    )
    private Instant createdAt;

    @Schema(
            description = "Informationen zum Projekttyp, BASIC oder SCRUM"
    )
    private ProjectType projectType;
}
