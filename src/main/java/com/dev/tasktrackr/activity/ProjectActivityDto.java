package com.dev.tasktrackr.activity;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Schema(name = "ProjectActivity", description = "Stellt eine einzelne Aktivität innerhalb eines Projekts dar")
public class ProjectActivityDto {

    @Schema(description = "ID der Aktivität")
    private Long id;

    @Schema(description = "Art der Aktivität. Z.B. USER_JOINED, FILE_UPLOADED, etc.")
    private ActivityType activityType;

    @Schema(description = "Name des Benutzers, der die Aktion ausgeführt hat", example = "john.doe")
    private String actorName;

    @Schema(description = "Name des Ziels der Aktion (z.B. der beigetretene Benutzer oder der Dateiname)", example = "jane.doe")
    private String targetName;

    @Schema(description = "Zusätzlicher Kontext zur Aktivität", example = "hat das Projekt betreten")
    private String context;

    @Schema(description = "Zeitpunkt, zu dem die Aktivität stattgefunden hat", example = "2025-10-03T10:53:00Z")
    private Instant createdAt;
}