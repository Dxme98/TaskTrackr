package com.dev.tasktrackr.scrumdetails.api.dtos.response;

import com.dev.tasktrackr.project.domain.enums.Priority;
import com.dev.tasktrackr.scrumdetails.domain.StoryStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
@Schema(name = "UserStoryResponse")
@AllArgsConstructor
@NoArgsConstructor
public class UserStoryResponseDto {

    @Schema(description = "ID der User Story", example = "101")
    private Long id;

    @Schema(description = "Titel der User Story")
    private String title;

    @Schema(description = "Nennt den Namen des Sprints, sofern die UserStory teil eines Sprints ist")
    private String sprintName;

    @Schema(description = "Detaillierte Beschreibung der User Story")
    private String description;

    @Schema(description = "Priorität der User Story. Mögliche Werte: LOW, MEDIUM, HIGH, URGENT")
    private Priority priority;

    @Schema(description = "Komplexitätsschätzung der User Story in Story Points", example = "8")
    private Integer storyPoints;

    @Schema(description = "Zeitpunkt der Erstellung", example = "2025-10-10T15:25:06Z")
    private Instant createdAt;

    @Schema(description = "Aktueller Status der User Story")
    private StoryStatus status;
}
