package com.dev.tasktrackr.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class WebClientConfig {

    @Value("${keycloak.internal-url}")
    private String keycloakUrl;

    @Bean
    public WebClient keyCloakWebClient(WebClient.Builder builder) {
        return builder
                .baseUrl(keycloakUrl)
                .build();
    }
}
