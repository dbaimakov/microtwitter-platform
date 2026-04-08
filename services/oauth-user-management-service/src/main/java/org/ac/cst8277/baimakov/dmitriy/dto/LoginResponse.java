package org.ac.cst8277.baimakov.dmitriy.dto;

import java.time.LocalDateTime;
import java.util.Set;

public record LoginResponse(
        String token,
        String jwtToken,
        LocalDateTime expiresAt,
        Long userId,
        String username,
        Set<String> roles,
        String authSource,
        String providerLogin
) {
}
