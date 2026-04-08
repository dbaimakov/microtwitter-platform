package org.ac.cst8277.baimakov.dmitriy.dto;

import java.util.List;

public record UserResponse(
        Long userId,
        String username,
        String displayName,
        String email,
        List<RoleResponse> roles,
        LastSessionResponse lastSession
) {
}
