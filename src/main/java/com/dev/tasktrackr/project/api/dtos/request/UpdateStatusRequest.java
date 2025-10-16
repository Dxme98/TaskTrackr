package com.dev.tasktrackr.project.api.dtos.request;

import com.dev.tasktrackr.project.domain.scrum.StoryStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UpdateStatusRequest {
    @NotNull
    private StoryStatus newStatus;
}
