package com.dev.tasktrackr.auth;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@Schema(name = "LoginResponse")
public class LoginResponse {

    @Schema(description = "JWT Access Token für die Authentifizierung",
            example = "eyJhbGciOiJIUzI1NiIsInR5cCI6...")
    private String accessToken;

    @Schema(description = "Der Benutzername des eingeloggten Users",
            example = "max.mustermann")
    private String username;
}
