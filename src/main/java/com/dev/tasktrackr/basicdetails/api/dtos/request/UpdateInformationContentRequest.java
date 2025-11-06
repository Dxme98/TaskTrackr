package com.dev.tasktrackr.basicdetails.api.dtos.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

@Schema(name = "UpdateInformationContentRequest")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UpdateInformationContentRequest {
    @Schema(
            description = "Selbst erstellte Projectinformationen",
            example = "Meine Projectinformationen",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    @Size(max = 100000)
    @NotNull
    private String content;
}
