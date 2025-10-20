package com.dev.tasktrackr.project.api.dtos.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class AssignMemberRequest {

    @Schema(
            description = "Die ID des Mitglieds, das zugewiesen werden soll.",
            example = "123",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NotNull(message = "MemberID ist erforderlich")
    private Long memberId;
}
