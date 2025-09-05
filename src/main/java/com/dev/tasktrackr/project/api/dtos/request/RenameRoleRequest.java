package com.dev.tasktrackr.project.api.dtos.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Schema(name = "RenameRoleRequest", description = "Request zum Umbenennen einer Projektrolle")
@Getter
@Setter
public class RenameRoleRequest {

    @Schema(
            description = "ID der Rolle, die umbenannt werden soll",
            example = "3",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NotNull(message = "Rollen-ID ist erforderlich")
    private Integer roleId;

    @Schema(
            description = "Neuer Name der Rolle",
            example = "Projektleiter",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NotBlank(message = "Rollenname darf nicht leer sein")
    @Size(max = 36)
    private String roleName;
}