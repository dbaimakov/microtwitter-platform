package org.ac.cst8277.baimakov.dmitriy.dto;

import java.time.LocalDateTime;
import java.util.Set;

public record JwtClaimsResponse(
        String issuer,
        String subject,
        String tokenId,
        Long userId,
        Set<String> roles,
        String authSource,
        String providerLogin,
        LocalDateTime issuedAt,
        LocalDateTime expiresAt
) {
}
