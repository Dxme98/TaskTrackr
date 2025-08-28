package com.dev.tasktrackr.project.api.dtos.request;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Schema(name = "ProjectInviteRequest")
@Getter
@Setter
public class ProjectInviteRequest {
    @Schema(
            requiredMode = Schema.RequiredMode.REQUIRED,
            minLength = 36,
            maxLength = 36,
            example = "46f6c4df-6303-41c4-b2dc-ec46c154xec4"
    )
    @NotBlank(message = "receiverId ist erforderlich")
    @Size(min = 36, max = 36, message = "ReceiverId muss 36 Zeichen lang sein")
    private String receiverId;
    @Schema(
            requiredMode = Schema.RequiredMode.REQUIRED,
            minLength = 36,
            maxLength = 36,
            example = "ID in the form of: 46f6c4df-6303-41c4-b2dc-ec46c154xec4"
    )
    @NotBlank(message = "senderId ist erforderlich")
    @Size(min = 36, max = 36, message = "SenderId muss 36 Zeichen lang sein")
    private String senderId;
    @Schema(
            requiredMode = Schema.RequiredMode.REQUIRED,
            example = "5"
    )
    @NotNull(message = "projectId ist erforderlich")
    private Long projectId;
}
