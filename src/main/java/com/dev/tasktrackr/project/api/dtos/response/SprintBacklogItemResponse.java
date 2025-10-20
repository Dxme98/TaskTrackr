package com.dev.tasktrackr.project.api.dtos.response;

import com.dev.tasktrackr.project.domain.enums.Priority;
import com.dev.tasktrackr.project.domain.scrum.StoryStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SprintBacklogItemResponse {

    @Schema(
            description = "Die eindeutige ID des Sprint Backlog Items.",
            example = "42"
    )
    private Long id; // sprintBacklogItemId

    @Schema(
            description = "Der Titel des Backlog Items (z.B. der User Story).",
            example = "Als Kunde möchte ich mein Passwort zurücksetzen können."
    )
    private String title;

    @Schema(
            description = "Die detaillierte Beschreibung oder Akzeptanzkriterien des Items.",
            example = "Der 'Passwort vergessen'-Link auf der Login-Seite soll eine E-Mail..."
    )
    private String description;

    @Schema(
            description = "Der geschätzte Aufwand für dieses Item in Story Points.",
            example = "5"
    )
    private int storyPoints;

    @Schema(
            description = "Die Priorität dieses Items.",
            example = "HIGH",
            allowableValues = {"LOW", "MEDIUM", "HIGH"}
    )
    private Priority priority;

    @Schema(
            description = "Der aktuelle Status des Items im Scrum Board.",
            example = "IN_PROGRESS",
            allowableValues = {"SPRINT_BACKLOG", "IN_PROGRESS", "REVIEW", "DONE"}
    )
    private StoryStatus status;

    @Schema(
            description = "Eine Liste von Kommentaren, die als 'Blocker' für dieses Item markiert sind."
    )
    private List<CommentResponseDto> blockers = new ArrayList<>();

    @Schema(
            description = "Eine Liste aller regulären Kommentare zu diesem Item."
    )
    private List<CommentResponseDto> comments = new ArrayList<>();

    @Schema(
            description = "Eine Liste der Benutzernamen (Assignees), die an diesem Item arbeiten."
    )
    private List<String> assignees = new ArrayList<>();
}
