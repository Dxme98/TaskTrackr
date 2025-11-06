package com.dev.tasktrackr.scrumdetails.api.dtos.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AssignMemberRequest {

    @Schema(
            description = "Die ID des Mitglieds, das zugewiesen werden soll.",
            example = "123",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NotNull(message = "MemberID ist erforderlich")
    private Long memberId;
}
