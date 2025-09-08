package com.dev.tasktrackr.auth;

import lombok.Data;

@Data
public class AuthResponse {
    private String accessToken;
    private String tokenType = "Bearer";

    AuthResponse (String accessToken) {
        this.accessToken = accessToken;
    }
}
