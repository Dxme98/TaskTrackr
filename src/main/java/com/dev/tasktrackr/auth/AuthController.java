package com.dev.tasktrackr.auth;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class AuthController {
    private final KeyCloakAuthService keyCloakAuthService;

    @PostMapping("/login")
    @ResponseStatus(HttpStatus.OK)
    String login(@RequestBody LoginRequest loginRequest) {
        return keyCloakAuthService.getToken(loginRequest);
    }
}