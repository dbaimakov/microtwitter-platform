package org.ac.cst8277.baimakov.dmitriy.controller;

import org.ac.cst8277.baimakov.dmitriy.dto.ApiResponse;
import org.ac.cst8277.baimakov.dmitriy.dto.AuthContextResponse;
import org.ac.cst8277.baimakov.dmitriy.service.AuthService;
import org.ac.cst8277.baimakov.dmitriy.service.UserService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/internal")
public class InternalAuthController {
    private final AuthService authService;
    private final UserService userService;

    public InternalAuthController(AuthService authService, UserService userService) {
        this.authService = authService;
        this.userService = userService;
    }
    @GetMapping("/auth/validate")
    public ApiResponse<AuthContextResponse> validateToken(@RequestParam String token) {
        return ApiResponse.ok(authService.validate(token), "Token has been vaidated successfully");
    }
    @GetMapping("/users/{userId}")
    public ApiResponse<AuthContextResponse> getUserInternal(@PathVariable Long userId) {
        return ApiResponse.ok(userService.getInternalUser(userId), "Internal user has been reuested successfully");
    }
}
