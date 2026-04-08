package org.ac.cst8277.baimakov.dmitriy.controller;

import org.ac.cst8277.baimakov.dmitriy.dto.ApiResponse;
import org.ac.cst8277.baimakov.dmitriy.dto.AuthContextResponse;
import org.ac.cst8277.baimakov.dmitriy.dto.JwtClaimsResponse;
import org.ac.cst8277.baimakov.dmitriy.dto.LoginRequest;
import org.ac.cst8277.baimakov.dmitriy.dto.LoginResponse;
import org.ac.cst8277.baimakov.dmitriy.service.AuthService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public ApiResponse<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        return ApiResponse.ok(authService.login(request), "Login has been requested successfully");
    }

    @GetMapping("/login/github")
    public ResponseEntity<Void> loginWithGithub() {
        return ResponseEntity.status(HttpStatus.FOUND)
                .location(URI.create("/oauth2/authorization/github"))
                .build();
    }

    @PostMapping("/logout")
    public ApiResponse<List<Object>> logout(@RequestHeader("X-Auth-Token") String token) {
        authService.logout(token);
        return ApiResponse.ok(List.of(), "Logout has been requested successfully");
    }

    @GetMapping("/me")
    public ApiResponse<AuthContextResponse> me(@RequestHeader("X-Auth-Token") String token) {
        return ApiResponse.ok(authService.currentUser(token), "Current user has been requested successfully");
    }

    @GetMapping("/jwt/inspect")
    public ApiResponse<JwtClaimsResponse> inspectJwt(@RequestParam String token) {
        return ApiResponse.ok(authService.inspectJwt(token), "JWT has been decoded successfully");
    }
}
