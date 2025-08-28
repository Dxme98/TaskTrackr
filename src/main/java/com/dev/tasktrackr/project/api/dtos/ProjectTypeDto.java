package com.dev.tasktrackr.project.api.dtos;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
@Schema(name = "ProjectType", description = "Typ des Projekts")
public class ProjectTypeDto {

    @Schema(description = "ID des Projekttyps", example = "1")
    private Integer id;

    @Schema(description = "Name des Projekttyps", example = "BASIC", allowableValues = {"BASIC", "SCRUM"})
    private String name;
}
