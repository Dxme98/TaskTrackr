package com.dev.tasktrackr.scrumdetails.api.dtos.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@Setter
@Schema(name = "CreateSprintRequest", description = "Datenobjekt zum Erstellen eines neuen Sprints")
public class CreateSprintRequest {

    @Schema(
            description = "Name des Sprints.",
            example = "Sprint Alpha - Q1 2025",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NotBlank(message = "Der Name darf nicht leer sein.")
    @Size(min = 3, max = 255, message = "Der Name muss zwischen 3 und 255 Zeichen lang sein.")
    private String name;

    @Schema(
            description = "Das Hauptziel des Sprints.",
            example = "Implementierung des initialen Registrierungs-Workflows.",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NotBlank(message = "Das Sprint-Ziel (goal) darf nicht leer sein.")
    @Size(min = 5, max = 500, message = "Das Ziel muss zwischen 5 und 500 Zeichen lang sein.")
    private String goal;

    @Schema(
            description = "Optionale Beschreibung oder Notizen für den Sprint.",
            example = "Fokus auf Backend-Validierung und CI/CD-Pipeline.",
            maxLength = 5000
    )
    @Size(max = 5000, message = "Die Beschreibung darf maximal 5000 Zeichen lang sein.")
    private String description;

    @Schema(
            description = "Startdatum des Sprints (YYYY-MM-DD). Darf nicht in der Vergangenheit liegen.",
            example = "2025-01-10",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NotNull(message = "Das Startdatum ist erforderlich.")
    @FutureOrPresent(message = "Das Startdatum muss heute oder in der Zukunft liegen.")
    private LocalDate startDate;

    @Schema(
            description = "Enddatum des Sprints (YYYY-MM-DD). Muss nach dem Startdatum liegen.",
            example = "2025-01-24",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NotNull(message = "Das Enddatum ist erforderlich.")
    private LocalDate endDate;

    @Schema(
            description = "Set von User Story IDs, die diesem Sprint bei Erstellung zugewiesen werden.",
            example = "[10, 15, 22]"
    )
    @NotNull(message = "Die Liste der UserStoryIds darf nicht null sein (kann aber leer sein).")
    private Set<Long> userStoryIds = new HashSet<>();

    // --- Klassen-Level Validierung ---

    @AssertTrue(message = "Das Enddatum (endDate) muss nach dem Startdatum (startDate) liegen.")
    @Schema(hidden = true)
    public boolean isEndDateAfterStartDate() {
        if (startDate == null || endDate == null) {
            return true;
        }
        return endDate.isAfter(startDate);
    }
}
