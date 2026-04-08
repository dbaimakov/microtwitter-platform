package org.ac.cst8277.baimakov.dmitriy.controller;

import org.ac.cst8277.baimakov.dmitriy.dto.ApiResponse;
import org.ac.cst8277.baimakov.dmitriy.dto.AuthContextResponse;
import org.ac.cst8277.baimakov.dmitriy.dto.CreateUserRequest;
import org.ac.cst8277.baimakov.dmitriy.dto.UserResponse;
import org.ac.cst8277.baimakov.dmitriy.service.AuthService;
import org.ac.cst8277.baimakov.dmitriy.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/v1/users")
public class UsersController {

    private final AuthService authService;
    private final UserService userService;

    public UsersController(AuthService authService, UserService userService) {
        this.authService = authService;
        this.userService = userService;
    }

    @GetMapping
    public ApiResponse<List<UserResponse>> getAllUsers(@RequestHeader("X-Auth-Token") String token) {
        authService.validate(token);
        return ApiResponse.ok(userService.findAll(), "List of users has been requested successfully");
    }

    @GetMapping("/{userId}")
    public ApiResponse<UserResponse> getUserById(
            @RequestHeader("X-Auth-Token") String token,
            @PathVariable Long userId
    ) {
        authService.validate(token);
        return ApiResponse.ok(userService.findById(userId), "User has been requested successfully");
    }

    @PostMapping
    public ResponseEntity<ApiResponse<UserResponse>> createUser(@Valid @RequestBody CreateUserRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created(userService.create(request), "User has been created successfully"));
    }

    @DeleteMapping("/{userId}")
    public ApiResponse<List<Object>> deleteUser(
            @RequestHeader("X-Auth-Token") String token,
            @PathVariable Long userId
    ) {
        AuthContextResponse auth = authService.validate(token);

        if (!auth.roles().contains("ADMIN")) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Insufficient role");
        }

        userService.delete(userId);
        return ApiResponse.ok(List.of(), "User has been deleted successfully");
    }
}
