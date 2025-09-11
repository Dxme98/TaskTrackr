package com.dev.tasktrackr.project.api.dtos.request;

import com.dev.tasktrackr.project.domain.enums.ProjectType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Schema(name = "ProjectRequest")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ProjectRequest {
    @Schema(
            example = "Mein Projektname",
            requiredMode = Schema.RequiredMode.REQUIRED,
            minLength = 3,
            maxLength = 255
    )
    @NotBlank(message = "Projektname ist erforderlich")
    @Size(min = 3, max = 255, message = "Projektname muss zwischen 3 und 255 Zeichen lang sein")
    private String name;

    @Schema(
            description = "Referenz auf den Projekttyp",
            example = "BASIC",
            allowableValues = {"BASIC", "SCRUM"},
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NotNull(message = "Projekttyp ist erforderlich")
    private ProjectType projectType;
}
