package com.dev.tasktrackr.project.api.dtos.request;

import com.dev.tasktrackr.project.domain.enums.Priority;
import com.dev.tasktrackr.project.domain.scrum.StoryStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Schema(name = "CreateUserStoryRequest", description = "Datenobjekt zum Erstellen einer neuen User Story")
@AllArgsConstructor
@NoArgsConstructor
public class CreateUserStoryRequest {

    @Schema(
            description = "Titel der User Story. Muss eindeutig sein und darf nicht leer sein.",
            example = "Als User möchte ich mich registrieren können.",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NotBlank(message = "Der Titel darf nicht leer sein.")
    @Size(min = 5, max = 255, message = "Der Titel muss zwischen 5 und 255 Zeichen lang sein.")
    private String title;

    @Schema(
            description = "Detaillierte Beschreibung der User Story (Akzeptanzkriterien, etc.).",
            example = "Der User gibt E-Mail, Benutzernamen und Passwort an. Nach der Registrierung wird eine Bestätigungs-E-Mail gesendet."
    )
    private String description; // Optional, da @Lob in der Entity nicht als not-null markiert ist

    @Schema(
            description = "Priorität der User Story.",
            example = "HIGH",
            requiredMode = Schema.RequiredMode.REQUIRED,
            allowableValues = {"LOW", "MEDIUM", "HIGH"}
    )
    @NotNull(message = "Die Priorität muss angegeben werden.")
    private Priority priority;

    @Schema(
            description = "Komplexitätsschätzung der User Story in Story Points.",
            example = "5",
            allowableValues = {"1", "2", "3", "5", "8", "13", "20", "40", "100"},
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    @Min(value = 0, message = "Story Points müssen 0 oder größer sein.")
    private Integer storyPoints;
}