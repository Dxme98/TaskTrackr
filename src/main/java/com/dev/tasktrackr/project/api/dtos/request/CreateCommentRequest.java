package com.dev.tasktrackr.project.api.dtos.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Data
public class CreateCommentRequest {

    @Schema(
            description = "Der Inhalt des Kommentars.",
            example = "Das sieht gut aus, bitte noch die Tests ergänzen.",
            maxLength = 500,
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NotBlank(message = "Message darf nicht leer sein")
    @Size(max = 500, message = "Message darf maximal 2000 Zeichen enthalten")
    String message;
}
