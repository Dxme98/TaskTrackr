package com.dev.tasktrackr.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.examples.Example;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.responses.ApiResponse;
import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("TaskTracker API")
                        .version("1.0.0"));
    }

    @Bean
    public OpenApiCustomizer openApiCustomizer() {
        return openApi -> {
            // Füge globale Responses zu allen Operationen hinzu
            openApi.getPaths().values().forEach(pathItem -> {
                pathItem.readOperations().forEach(operation -> {
                    // Füge 401 Unauthorized zu allen gesicherten Operationen hinzu
                    if (isSecuredEndpoint(operation)) {
                        if (operation.getResponses().get("401") == null) {
                            operation.getResponses().addApiResponse("401",
                                    new ApiResponse().$ref("#/components/responses/Unauthorized"));
                        }
                    }

                    // Füge 500 Internal Server Error zu allen Operationen hinzu
                    if (operation.getResponses().get("500") == null) {
                        operation.getResponses().addApiResponse("500",
                                new ApiResponse().$ref("#/components/responses/InternalServerError"));
                    }

                    // Füge 400 Validation Error zu POST/PUT/PATCH Operationen hinzu
                    if (isValidationEndpoint(operation)) {
                        if (operation.getResponses().get("400") == null) {
                            operation.getResponses().addApiResponse("400",
                                    new ApiResponse().$ref("#/components/responses/ValidationError"));
                        }
                    }
                });
            });
        };
    }

    private boolean isValidationEndpoint(io.swagger.v3.oas.models.Operation operation) {
        // Prüfe, ob die Operation ein RequestBody hat (POST/PUT/PATCH)
        return operation.getRequestBody() != null;
    }

    private boolean isSecuredEndpoint(io.swagger.v3.oas.models.Operation operation) {
        // In Spring Boot mit Spring Security sind standardmäßig alle Endpoints gesichert,
        // es sei denn, sie sind explizit als permitAll konfiguriert
        // Da Ihr Controller @PreAuthorize hat, sind alle Endpoints gesichert
        return operation.getSecurity() != null && !operation.getSecurity().isEmpty();

        // Alternative: Prüfung auf Security-Annotationen
        // return operation.getSecurity() != null && !operation.getSecurity().isEmpty();
    }
}
