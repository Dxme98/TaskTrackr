package com.dev.tasktrackr.project.api.dtos.request;

import com.dev.tasktrackr.project.domain.enums.PermissionName;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.util.Set;

@Schema(name = "ProjectRoleRequest", description = "Request-Daten für das Erstellen einer Projektrolle")
@Getter
@Setter
public class BasicProjectRoleRequest {
    @Schema(
            description = "Name der Projektrolle",
            example = "Rollenname",
            requiredMode = Schema.RequiredMode.REQUIRED,
            minLength = 3,
            maxLength = 36
    )
    @NotBlank(message = "Rollenname ist erforderlich")
    @Size(min = 3, max = 36, message = "Rollenname muss zwischen 3 und 36 Zeichen lang sein")
    private String name;

    @Schema(
            description = "Liste von Berechtigungen für die Rolle. " +
                    "Erlaubte Werte sind aus dem PermissionName-Enum.",
            example = "[\"BASIC_CREATE_TASK\", \"COMMON_INVITE_USER\"]",
            requiredMode = Schema.RequiredMode.REQUIRED,
            allowableValues = {"BASIC_CREATE_TASK", "BASIC_DELETE_TASK", "BASIC_EDIT_INFORMATION",
                    "COMMON_INVITE_USER", "COMMON_REMOVE_USER", "COMMON_MANAGE_ROLES"}
    )
    @NotEmpty(message = "Mindestens eine Berechtigung muss ausgewählt werden")
    private Set<PermissionName> permissions;
}
