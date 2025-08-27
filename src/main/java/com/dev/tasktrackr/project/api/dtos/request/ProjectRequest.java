package com.dev.tasktrackr.project.api.dtos.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Schema(name = "ProjectRequest")
@Getter
@Setter
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
            description = "Referenz auf den Projekttyp (1:BASIC, 2:SCRUM)",
            example = "1",
            allowableValues = {"1", "2"},
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NotNull(message = "Projekttyp ist erforderlich")
    @Min(value = 1, message = "Ungültige Projekttyp-ID")
    private int projectTypeId;
}
