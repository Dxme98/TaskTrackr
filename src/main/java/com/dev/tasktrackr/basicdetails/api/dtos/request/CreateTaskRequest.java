package com.dev.tasktrackr.basicdetails.api.dtos.request;

import com.dev.tasktrackr.project.domain.enums.Priority;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Schema(name = "CreateTaskRequest")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CreateTaskRequest {

    @Schema(
            description = "Titel der Aufgabe",
            example = "Implementiere Login-Feature",
            minLength = 3,
            maxLength = 255,
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NotBlank(message = "Titel ist erforderlich")
    @Size(min = 3, max = 255, message = "Titel muss zwischen 3 und 255 Zeichen lang sein")
    private String title;

    @Schema(
            description = "Beschreibung der Aufgabe (max. 2000 Zeichen)",
            example = "Die Aufgabe umfasst die Implementierung des Login-Workflows inkl. JWT.",
            maxLength = 2000,
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NotBlank(message = "Beschreibung ist erforderlich")
    @Size(max = 2000, message = "Beschreibung darf maximal 2000 Zeichen enthalten")
    private String description;

    @Schema(
            description = "Priorität der Aufgabe",
            example = "HIGH",
            allowableValues = {"LOW", "MEDIUM", "HIGH"},
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NotNull(message = "Priorität ist erforderlich")
    private Priority priority;

    @Schema(
            description = "Fälligkeitsdatum der Aufgabe",
            example = "2025-12-31T23:59:59"
    )
    @Future(message = "Das Fälligkeitsdatum muss in der Zukunft liegen")
    private LocalDateTime dueDate;

    @Schema(
            description = "IDs der zugewiesenen Projektmitglieder, nur diese und der Ersteller können die Aufgabe abschließen",
            example = "[1, 5, 10]",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NotEmpty
    private Set<Long> assignedToMemberIds = new HashSet<>();
}
