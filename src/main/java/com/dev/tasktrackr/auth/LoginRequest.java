package com.dev.tasktrackr.auth;

import lombok.Data;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Schema(name = "LoginRequest")
@Getter
@Setter
public class LoginRequest {

    @Schema(
            description = "Benutzername für den Login",
            requiredMode = Schema.RequiredMode.REQUIRED,
            minLength = 3,
            maxLength = 50,
            example = "max.mustermann"
    )
    @NotBlank(message = "Username ist erforderlich")
    @Size(min = 3, max = 50, message = "Username muss zwischen 3 und 50 Zeichen lang sein")
    private String username;

    @Schema(
            description = "Passwort für den Login",
            requiredMode = Schema.RequiredMode.REQUIRED,
            minLength = 6,
            maxLength = 100,
            example = "Geheim123!"
    )
    @NotBlank(message = "Passwort ist erforderlich")
    @Size(min = 6, max = 100, message = "Passwort muss zwischen 6 und 100 Zeichen lang sein")
    private String password;
}