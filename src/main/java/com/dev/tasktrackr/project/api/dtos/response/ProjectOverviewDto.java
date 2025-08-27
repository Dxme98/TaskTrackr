package com.dev.tasktrackr.project.api.dtos.response;

import com.dev.tasktrackr.project.api.dtos.ProjectTypeDto;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
@Schema(name = "ProjectResponse")
@Builder
public class ProjectOverviewDto {
    @Schema(example = "456")
    private Long id;

    @Schema(example = "Mein Projektname")
    private String name;

    @Schema(
            description = "Zeitpunkt der Projekterstellung im ISO-8601 Format",
            example = "2024-01-15T14:30:00"
    )
    private Instant createdAt;

    // n+1
    @Schema(description = "Projekttyp Informationen")
    private ProjectTypeDto projectType;

    // Load ONLY when opened! Useless at the overview.
    /**
     @Schema(description = "Liste aller Projektmitglieder")
     private List<ProjectMemberDto> projectMembers;
     */
}
