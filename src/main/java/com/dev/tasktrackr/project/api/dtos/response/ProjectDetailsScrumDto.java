package com.dev.tasktrackr.project.api.dtos.response;

import com.dev.tasktrackr.project.api.dtos.ProjectMemberDto;
import com.dev.tasktrackr.project.api.dtos.ProjectTypeDto;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@Builder
public class ProjectDetailsScrumDto implements ProjectDetailsDto {
    @Schema(example = "456")
    private Long id;

    @Schema(example = "Mein Projektname")
    private String name;

    @Schema(description = "Projekttyp Informationen")
    private ProjectTypeDto projectType;

    @Schema(description = "Liste aller Projektmitglieder")
    private List<ProjectMemberDto> projectMembers; // Später Rollen adden
}
