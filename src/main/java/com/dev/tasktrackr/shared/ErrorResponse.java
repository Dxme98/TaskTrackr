package com.dev.tasktrackr.shared;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Standard error response")
public class ErrorResponse {

    @Schema(description = "Zeitpunkt des Fehlers", example = "2025-08-22T12:34:56")
    private LocalDateTime timestamp;

    @Schema(description = "HTTP Statuscode", example = "401")
    private int status;

    @Schema(description = "Anwendungsspezifischer Fehlercode", example = "NO_WRITE_PERMISSION")
    private ErrorCode code;

    @Schema(description = "Fehlermeldung", example = "Authentication token is missing or invalid")
    private String message;

    @Schema(description = "Request path", example = "/api/v1/projects")
    private String path;
}
