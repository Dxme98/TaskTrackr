package com.dev.tasktrackr.shared;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.Map;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Validation error response with field-specific errors")
public class ValidationErrorResponse extends ErrorResponse {
    @Schema(description = "Fehlermeldungen pro Feld")
    private Map<String, String> fieldErrors;


    public ValidationErrorResponse(LocalDateTime timestamp, int status, ErrorCode errorCode, String message, String path, Map<String, String> fieldErrors) {
        super(timestamp, status, errorCode, message, path);
        this.fieldErrors = fieldErrors;
    }
}


