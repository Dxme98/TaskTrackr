package com.dev.tasktrackr.basicdetails.api.dtos.request;

import com.dev.tasktrackr.basicdetails.domain.LinkType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

@Schema(name = "CreateLinkRequest")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CreateLinkRequest {
    @Schema(
            description = "Anzeigename",
            example = "Github Repository",
            maxLength = 80,
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NotBlank(message = "Title ist erforderlich")
    @Size(max = 80, message = "Title darf maximal 80 Zeichen enthalten")
    private String title;
    @Schema(
            description = "Die URL zur Website",
            example = "https://www.google.com/",
            maxLength = 500,
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NotBlank(message = "URL ist erforderlich")
    @Size(max = 500, message = "URL darf maximal 500 Zeichen enthalten")
    private String url;
    @Schema(
            description = "Linktype der URL ",
            example = "GITHUB",
            allowableValues = {"GITHUB", "DOC", "WEB"},
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NotNull(message = "LinkType ist erforderlich")
    private LinkType linkType;
}
