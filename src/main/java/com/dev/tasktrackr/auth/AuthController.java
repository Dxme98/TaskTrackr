package com.dev.tasktrackr.auth;

import com.dev.tasktrackr.shared.exception.model.ErrorResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class AuthController {

    private final KeyCloakAuthService keyCloakAuthService;

    @PostMapping("/login")
    @Operation(
            summary = "Login with username and password",
            description = "Authenticates the user against Keycloak and returns an access token together with the username"
    )
    @ApiResponse(
            responseCode = "200",
            description = "Login successful",
            content = @Content(schema = @Schema(implementation = LoginResponse.class))
    )
    @ApiResponse(
            responseCode = "401",
            description = "Invalid username or password",
            content = @Content(
                    schema = @Schema(implementation = ErrorResponse.class),
                    examples = {
                            @ExampleObject(
                                    name = "Unauthorized Example",
                                    value = """
                                    {
                                      "timestamp": "2025-09-08T19:20:30",
                                      "status": 401,
                                      "code": "UNAUTHORIZED",
                                      "message": "Invalid username or password",
                                      "path": "/api/v1/login"
                                    }
                                    """
                            )
                    }
            )
    )
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest loginRequest) {
        String token = keyCloakAuthService.getToken(loginRequest);
        LoginResponse response = new LoginResponse(token, loginRequest.getUsername());

        return ResponseEntity.status(HttpStatus.OK).body(response);
    }
}