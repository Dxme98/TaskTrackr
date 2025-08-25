package com.dev.tasktrackr.shared.api.annotation;

import com.dev.tasktrackr.shared.exception.model.ErrorResponse;
import com.dev.tasktrackr.shared.exception.model.ValidationErrorResponse;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

public final class ApiErrorResponses {

    @Target({ElementType.METHOD, ElementType.TYPE})
    @Retention(RetentionPolicy.RUNTIME)
    @ApiResponses({
            @ApiResponse(
                    responseCode = "400",
                    description = "Bad Request - Validation failed",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ValidationErrorResponse.class),
                            examples = {
                                    @ExampleObject(
                                            name = "ValidationError",
                                            summary = "Validation error example",
                                            value = """
                                            {
                                              "timestamp": "2025-08-23T10:15:30",
                                              "status": 400,
                                              "code": "VALIDATION_ERROR",
                                              "message": "Validation failed for request body",
                                              "path": "/api/v1/projects",
                                              "fieldErrors": {
                                                "name": "Project name must not be blank",
                                                "email": "Must be a valid email address"
                                              }
                                            }
                                            """
                                    )
                            }
                    )
            )
    })
    public @interface BadRequest {
    }

    @Target({ElementType.METHOD, ElementType.TYPE})
    @Retention(RetentionPolicy.RUNTIME)
    @ApiResponses({
            @ApiResponse(
                    responseCode = "401",
                    description = "Unauthorized - Authentication required",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = {
                                    @ExampleObject(
                                            name = "Unauthorized",
                                            summary = "Authentication required",
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
    })
    public @interface Unauthorized {
    }

    @Target({ElementType.METHOD, ElementType.TYPE})
    @Retention(RetentionPolicy.RUNTIME)
    @ApiResponses({
            @ApiResponse(
                    responseCode = "403",
                    description = "Forbidden - Insufficient permissions",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = {
                                    @ExampleObject(
                                            name = "Forbidden",
                                            summary = "Access denied",
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
    })
    public @interface Forbidden {
    }

    @Target({ElementType.METHOD, ElementType.TYPE})
    @Retention(RetentionPolicy.RUNTIME)
    @ApiResponses({
            @ApiResponse(
                    responseCode = "404",
                    description = "Not Found - Resource does not exist",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = {
                                    @ExampleObject(
                                            name = "NotFound",
                                            summary = "Resource not found",
                                            value = """
                                            {
                                              "timestamp": "2025-08-23T10:15:30",
                                              "status": 404,
                                              "code": "RESOURCE_NOT_FOUND",
                                              "message": "Project with id 123 not found",
                                              "path": "/api/v1/projects/123"
                                            }
                                            """
                                    )
                            }
                    )
            )
    })
    public @interface NotFound {
    }

    @Target({ElementType.METHOD, ElementType.TYPE})
    @Retention(RetentionPolicy.RUNTIME)
    @ApiResponses({
            @ApiResponse(
                    responseCode = "500",
                    description = "Internal Server Error - Unexpected error occurred",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = {
                                    @ExampleObject(
                                            name = "InternalServerError",
                                            summary = "Server error",
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
    })
    public @interface InternalServerError {
    }


    // Kombinierte Annotationen für häufige Kombinationen
    @Target({ElementType.METHOD, ElementType.TYPE})
    @Retention(RetentionPolicy.RUNTIME)
    @Unauthorized
    @InternalServerError
    public @interface CommonErrors {
    }

    @Target({ElementType.METHOD, ElementType.TYPE})
    @Retention(RetentionPolicy.RUNTIME)
    @BadRequest
    @Unauthorized
    @InternalServerError
    public @interface ValidationEndpoint {
    }

    @Target({ElementType.METHOD, ElementType.TYPE})
    @Retention(RetentionPolicy.RUNTIME)
    @Unauthorized
    @Forbidden
    @NotFound
    @InternalServerError
    public @interface SecuredResourceEndpoint {
    }
}
