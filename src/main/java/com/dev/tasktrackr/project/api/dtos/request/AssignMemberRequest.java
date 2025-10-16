package com.dev.tasktrackr.project.api.dtos.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class AssignMemberRequest {
    @NotNull
    private Long memberId;
}
