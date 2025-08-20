package com.dev.tasktrackr.auth;

import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
public class AuthController {

    @GetMapping("/open")
    @ResponseStatus(HttpStatus.OK)
    public String open() {
        return "Open Endpoint";
    }

    @GetMapping("/closed")
    @ResponseStatus(HttpStatus.OK)
    public String secured(@AuthenticationPrincipal Jwt jwt) {
        SecurityContextHolder.getContext();

        return "Secured Endpoint" + " username: " + jwt.getClaim("preferred_username") + " realm access:" + jwt.getClaim("realm_access") +
                " resource_access: " + jwt.getClaim("resource_access");
    }

    @GetMapping("/admin")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public String adminSecured(@AuthenticationPrincipal Jwt jwt) {
        return "Admin Secured Endpoint" + " username: " + jwt.getClaim("preferred_username") + "Contextholder: " + SecurityContextHolder.getContext();
    }



}
