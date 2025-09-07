package com.dev.tasktrackr.project.api.dtos.response;

import com.dev.tasktrackr.project.domain.enums.PermissionName;
import com.dev.tasktrackr.project.domain.enums.RoleType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.util.Set;

@Getter
@Setter
@Builder
@Schema(name = "ProjectRoleResponse", description = "Antwort mit Informationen zu einer Projektrolle")
@AllArgsConstructor
public class ProjectRoleResponse {

    @Schema(description = "ID der Rolle", example = "3")
    private Integer id;


    @Schema(description = "Name der Rolle", example = "Projektleiter")
    private String name;

    @Schema(description = "ID des Projekts, zu dem die Rolle gehört", example = "456")
    private Long projectId;

    @Schema(description = "Berechtigungen, die dieser Rolle zugeordnet sind", example = "[\"BASIC_CREATE_TASK\", \"COMMON_INVITE_USER\"]")
    private Set<PermissionName> permissions;

    @Schema(description = "Typ der Rolle,  (BASE, OWNER, CUSTOM)")
    private RoleType roleType;
}