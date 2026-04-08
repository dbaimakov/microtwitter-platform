package org.ac.cst8277.baimakov.dmitriy.controller;

import org.ac.cst8277.baimakov.dmitriy.dto.ApiResponse;
import org.ac.cst8277.baimakov.dmitriy.dto.RoleResponse;
import org.ac.cst8277.baimakov.dmitriy.service.AuthService;
import org.ac.cst8277.baimakov.dmitriy.service.RoleService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/roles")
public class RolesController {
    private final AuthService authService;
    private final RoleService roleService;

    public RolesController(AuthService authService, RoleService roleService) {
        this.authService = authService;
        this.roleService = roleService;
    }
    @GetMapping
    public ApiResponse<List<RoleResponse>> getAllRoles(@RequestHeader("X-Auth-Token") String token) {
        authService.validate(token);
        return ApiResponse.ok(roleService.findAll(), "List of roles has been reqested successfully");
    }
}
