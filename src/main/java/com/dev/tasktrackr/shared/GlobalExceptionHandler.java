package com.dev.tasktrackr.shared;

import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ApiResponse(
            responseCode = "400",
            description = "Invalid request data - Validation failed for request body",
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = ValidationErrorResponse.class),
                    examples = {
                            @ExampleObject(
                                    name = "ValidationErrorExample",
                                    summary = "Example for validation errors",
                                    value = """
                    {
                      "timestamp": "2025-08-23T10:15:30",
                      "status": 400,
                      "code": "VALIDATION_ERROR",
                      "message": "Validation failed for request body",
                      "path": "/api/v1/projects",
                      "fieldErrors": {
                        "name": "Project name must not be blank",
                        "email": "Must be a valid email address",
                        "startDate": "Start date cannot be in the past"
                      }
                    }
                    """
                            )
                    }
            )
    )
    public ResponseEntity<ErrorResponse> handleValidationErrors(MethodArgumentNotValidException ex, HttpServletRequest request) {
        Map<String, String> errors = new HashMap<>();

        ex.getBindingResult().getFieldErrors().forEach(error ->
                errors.put(error.getField(), error.getDefaultMessage())
        );

        ErrorResponse error = new ValidationErrorResponse(
                LocalDateTime.now(),
                HttpStatus.BAD_REQUEST.value(),
                ErrorCode.VALIDATION_ERROR,
                "Validation failed for request body",
                request.getRequestURI(),
                errors
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    // Handler für fehlende Request Parameter
    @ExceptionHandler(MissingServletRequestParameterException.class)
    @ApiResponse(
            responseCode = "400",
            description = "Missing required request parameter",
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = ErrorResponse.class),
                    examples = {
                            @ExampleObject(
                                    name = "MissingParameterExample",
                                    summary = "Example for missing parameter",
                                    value = """
                    {
                      "timestamp": "2025-08-23T10:15:30",
                      "status": 400,
                      "code": "VALIDATION_ERROR",
                      "message": "Required parameter 'projectId' is missing",
                      "path": "/api/v1/projects"
                    }
                    """
                            )
                    }
            )
    )
    public ResponseEntity<ErrorResponse> handleMissingServletRequestParameter(
            MissingServletRequestParameterException ex, HttpServletRequest request) {

        ErrorResponse error = new ErrorResponse(
                LocalDateTime.now(),
                HttpStatus.BAD_REQUEST.value(),
                ErrorCode.VALIDATION_ERROR,
                String.format("Required parameter '%s' is missing", ex.getParameterName()),
                request.getRequestURI()
        );

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    // Handler für falsche Parametertypen
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    @ApiResponse(
            responseCode = "400",
            description = "Invalid parameter type",
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = ErrorResponse.class),
                    examples = {
                            @ExampleObject(
                                    name = "TypeMismatchExample",
                                    summary = "Example for type mismatch",
                                    value = """
                    {
                      "timestamp": "2025-08-23T10:15:30",
                      "status": 400,
                      "code": "VALIDATION_ERROR",
                      "message": "Parameter 'id' should be of type Long but received 'abc'",
                      "path": "/api/v1/projects/abc"
                    }
                    """
                            )
                    }
            )
    )
    public ResponseEntity<ErrorResponse> handleTypeMismatch(
            MethodArgumentTypeMismatchException ex, HttpServletRequest request) {

        String message = String.format("Parameter '%s' should be of type %s but received '%s'",
                ex.getName(),
                ex.getRequiredType() != null ? ex.getRequiredType().getSimpleName() : "unknown",
                ex.getValue());

        ErrorResponse error = new ErrorResponse(
                LocalDateTime.now(),
                HttpStatus.BAD_REQUEST.value(),
                ErrorCode.VALIDATION_ERROR,
                message,
                request.getRequestURI()
        );

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    @ExceptionHandler(ForbiddenException.class)
    @ApiResponse(
            responseCode = "403",
            description = "Forbidden – User is authenticated but lacks the necessary permissions", // Beschreibung korrigiert
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = ErrorResponse.class),
                    examples = {
                            @ExampleObject(
                                    name = "ForbiddenExample",
                                    summary = "Example for 403 Forbidden",
                                    value = """
                {
                  "timestamp": "2025-08-23T10:15:30",
                  "status": 403,
                  "code": "NO_WRITE_PERMISSION",
                  "message": "Insufficient permissions to access this resource",
                  "path": "/api/v1/projects"
                }
                """
                            )
                    }
            )
    )
    public ResponseEntity<ErrorResponse> handleForbidden(
            ForbiddenException ex, HttpServletRequest request) {

        ErrorResponse error = new ErrorResponse(
                LocalDateTime.now(),
                HttpStatus.FORBIDDEN.value(),
                ex.getErrorCode(),
                ex.getMessage(),
                request.getRequestURI()
        );

        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
    }



    @ExceptionHandler(AuthenticationException.class)
    @ApiResponse(
            responseCode = "401",
            description = "Unauthorized - Authentication failed",
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = ErrorResponse.class),
                    examples = {
                            @ExampleObject(
                                    name = "UnauthorizedExample",
                                    summary = "Example for 401 Unauthorized",
                                    value = """
                    {
                      "timestamp": "2025-08-23T10:15:30",
                      "status": 401,
                      "code": "UNAUTHORIZED",
                      "message": "Authentication token is missing or invalid",
                      "path": "/api/v1/projects"
                    }
                    """
                            )
                    }
            )
    )
    public ResponseEntity<ErrorResponse> handleAuthenticationException(
            AuthenticationException ex, HttpServletRequest request) {

        ErrorResponse error = new ErrorResponse(
                LocalDateTime.now(),
                HttpStatus.UNAUTHORIZED.value(),
                ErrorCode.UNAUTHORIZED,
                "Authentication token is missing or invalid",
                request.getRequestURI()
        );

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
    }

    // Generischer Handler für alle anderen Exceptions
    @ExceptionHandler(Exception.class)
    @ApiResponse(
            responseCode = "500",
            description = "Internal server error",
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = ErrorResponse.class),
                    examples = {
                            @ExampleObject(
                                    name = "InternalServerErrorExample",
                                    summary = "Example for internal server error",
                                    value = """
                    {
                      "timestamp": "2025-08-23T10:15:30",
                      "status": 500,
                      "code": "INTERNAL_ERROR",
                      "message": "An unexpected error occurred",
                      "path": "/api/v1/projects"
                    }
                    """
                            )
                    }
            )
    )
    public ResponseEntity<ErrorResponse> handleGenericException(
            Exception ex, HttpServletRequest request) {

        // Log the full exception for debugging
        // log.error("Unexpected error occurred", ex);

        ErrorResponse error = new ErrorResponse(
                LocalDateTime.now(),
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                ErrorCode.INTERNAL_ERROR,
                "An unexpected error occurred",
                request.getRequestURI()
        );

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }
}

