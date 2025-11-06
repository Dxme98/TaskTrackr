package com.dev.tasktrackr.basicdetails.api.dtos.response;

import com.dev.tasktrackr.scrumdetails.domain.CommentType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class CommentResponseDto {

    @Schema(
            description = "Die eindeutige ID des Kommentars.",
            example = "101"
    )
    private Long id;

    @Schema(
            description = "Die ID des Sprint Backlog Items, zu dem der Kommentar gehört.",
            example = "45"
    )
    private Long sprintBacklogItemId;

    @Schema(
            description = "Der Benutzername der Person, die den Kommentar erstellt hat.",
            example = "max.mustermann"
    )
    private String createdByUsername;

    @Schema(
            description = "Der Inhalt (Text) des Kommentars.",
            example = "Die Akzeptanzkriterien sind noch nicht vollständig."
    )
    private String message;

    @Schema(
            description = "Die Art des Kommentars (z.B. ob vom System generiert oder ein Benutzerkommentar).",
            example = "COMMENT",
            allowableValues = {"COMMENT", "BLOCKER"}
    )
    private CommentType type;

    @Schema(
            description = "Der genaue Zeitstempel, wann der Kommentar erstellt wurde (im ISO 8601 Format).",
            example = "2024-10-28T14:30:15Z"
    )
    private Instant createdAt;
}
