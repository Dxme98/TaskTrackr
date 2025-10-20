package com.dev.tasktrackr.project.api.dtos.request;

import com.dev.tasktrackr.project.domain.scrum.StoryStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import io.swagger.v3.oas.annotations.media.Schema;

@Data
public class UpdateStatusRequest {

    @Schema(
            description = "Der neue Status der Story.",
            example = "IN_PROGRESS",
            allowableValues = { "SPRINT_BACKLOG", "IN_PROGRESS", "REVIEW", "DONE"},
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NotNull(message = "NewStatus ist erforderlich")
    private StoryStatus newStatus;
}
