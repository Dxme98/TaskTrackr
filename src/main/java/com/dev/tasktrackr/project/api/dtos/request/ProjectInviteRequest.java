package com.dev.tasktrackr.project.api.dtos.request;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Schema(name = "ProjectInviteRequest")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ProjectInviteRequest {
    @Schema(
            requiredMode = Schema.RequiredMode.REQUIRED,
            maxLength = 36,
            example = "BeispielUsername"
    )
    @NotBlank(message = "receiverUsername ist erforderlich")
    @Size( max = 32, message = "ReceiverUsername darf maximal 36 Zeichen lang sein")
    private String receiverUsername;
}
