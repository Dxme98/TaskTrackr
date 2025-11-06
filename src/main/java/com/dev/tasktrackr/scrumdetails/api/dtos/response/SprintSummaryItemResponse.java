package com.dev.tasktrackr.scrumdetails.api.dtos.response;


import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class SprintSummaryItemResponse {

    @Schema(
            description = "Die eindeutige ID des Backlog Items.",
            example = "42"
    )
    private Long id;

    @Schema(
            description = "Der Titel des Backlog Items.",
            example = "Als Kunde möchte ich mein Passwort zurücksetzen können."
    )
    private String title;

    @Schema(
            description = "Der geschätzte Aufwand in Story Points.",
            example = "5"
    )
    private Integer storyPoints;

    @Schema(
            description = "Gibt an, ob das Item als 'Done' (abgeschlossen) markiert ist.",
            example = "false"
    )
    private boolean isCompleted;
}
