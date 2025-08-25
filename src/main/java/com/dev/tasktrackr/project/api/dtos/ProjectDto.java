package com.dev.tasktrackr.project.api.dtos;

import com.dev.tasktrackr.project.domain.ProjectType;
import com.dev.tasktrackr.user.UserDto;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.*;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;

public class ProjectDto {
    @Schema(name = "ProjectRequest")
    @Getter
    @Setter
    public static class Request {
        @Schema(
                example = "Mein Projektname",
                requiredMode = Schema.RequiredMode.REQUIRED,
                minLength = 3,
                maxLength = 255
        )
        @NotBlank(message = "Projektname ist erforderlich")
        @Size(min = 3, max = 255, message = "Projektname muss zwischen 3 und 255 Zeichen lang sein")
        private String name;

        @Schema(
                description = "Referenz auf den Projekttyp (1:BASIC, 2:SCRUM)",
                example = "1",
                allowableValues = {"1", "2"},
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        @NotNull(message = "Projekttyp ist erforderlich")
        @Min(value = 1, message = "Ungültige Projekttyp-ID")
        private int projectTypeId;
    }

    @Getter
    @Setter
    @Schema(name = "ProjectResponse")
    @Builder
    public static class Response {
        @Schema(example = "456")
        private Long id;

        @Schema(example = "Mein Projektname")
        private String name;

        @Schema(
                description = "Zeitpunkt der Projekterstellung im ISO-8601 Format",
                example = "2024-01-15T14:30:00"
        )
        private Instant createdAt;

        @Schema(description = "Ersteller des Projekts")
        private UserDto creator;

        @Schema(description = "Projekttyp Informationen")
        private ProjectTypeDto projectType;

        @Schema(description = "Liste aller Projektmitglieder")
        private List<ProjectMemberDto> projectMembers;
    }
}