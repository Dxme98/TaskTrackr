package com.dev.tasktrackr.project.api.dtos.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class InviteStatusResponseDto {
    @Schema(description = "ID des Einladungstatus", example = "1")
    private Integer id;

    @Schema(description = "Name des Einladungstatus", example = "PENDING", allowableValues = {"PENDING", "ACCEPTED", "DECLINED"})
    private String name;
}
