package com.dev.tasktrackr.auth;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Map;

@Service
public class KeyCloakAuthService {
    private final WebClient webClient;
    @Value("${KEYCLOAK_CLIENT_SECRET}")
    private String clientSecret;
    @Value("${KEYCLOAK_CLIENT_ID}")
    private String clientId;

    public KeyCloakAuthService(WebClient keyCloakWebClient) {
        this.webClient = keyCloakWebClient;
    }

    public String getToken(LoginRequest loginRequest) {
        MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        form.add("grant_type", "password");
        form.add("client_id", clientId);
        form.add("client_secret", clientSecret);
        form.add("username", loginRequest.getUsername());
        form.add("password", loginRequest.getPassword());

        // Sende POST-Request an /token
        Map<String, Object> response = webClient.post()
                .uri("/token")
                .bodyValue(form)
                .retrieve()
                .bodyToMono(Map.class)
                .block();

        if (response == null || !response.containsKey("access_token")) {
            throw new RuntimeException("Login failed");
        }

        return (String) response.get("access_token");
    }
}
