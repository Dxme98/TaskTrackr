package com.dev.tasktrackr.project.api.dtos.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Schema(name = "AssignRoleRequest", description = "Request für die Zuweisung einer Rolle an ein Projektmitglied")
@Getter
@Setter
public class AssignRoleRequest {

    @Schema(
            description = "ID des Projektmitglieds, dem eine neue Rolle zugewiesen werden soll",
            example = "42",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NotNull(message = "Projektmitglied-ID ist erforderlich")
    private Long projectMemberId;

    @Schema(
            description = "ID der Rolle, die zugewiesen werden soll",
            example = "3",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NotNull(message = "Rollen-ID ist erforderlich")
    private int roleId;
}