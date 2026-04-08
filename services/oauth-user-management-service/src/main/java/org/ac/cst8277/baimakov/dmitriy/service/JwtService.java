package org.ac.cst8277.baimakov.dmitriy.service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.ac.cst8277.baimakov.dmitriy.config.AppSecurityProperties;
import org.ac.cst8277.baimakov.dmitriy.dto.JwtClaimsResponse;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import javax.annotation.PostConstruct;
import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Collection;
import java.util.Date;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.UUID;

@Service
public class JwtService {

    private final AppSecurityProperties appSecurityProperties;
    private SecretKey secretKey;

    public JwtService(AppSecurityProperties appSecurityProperties) {
        this.appSecurityProperties = appSecurityProperties;
    }

    @PostConstruct
    public void init() {
        this.secretKey = Keys.hmacShaKeyFor(appSecurityProperties.getJwt().getSecret().getBytes(StandardCharsets.UTF_8));
    }

    public String issueToken(
            Long userId,
            String username,
            Set<String> roles,
            LocalDateTime issuedAt,
            LocalDateTime expiresAt,
            String authSource,
            String providerLogin
    ) {
        var builder = Jwts.builder()
                .issuer(appSecurityProperties.getJwt().getIssuer())
                .subject(username)
                .id(UUID.randomUUID().toString())
                .issuedAt(toDate(issuedAt))
                .expiration(toDate(expiresAt))
                .claim("uid", userId)
                .claim("roles", roles)
                .claim("auth_source", authSource);

        if (providerLogin != null && !providerLogin.isBlank()) {
            builder.claim("provider_login", providerLogin);
        }

        return builder.signWith(secretKey).compact();
    }

    public JwtClaimsResponse inspect(String token) {
        try {
            Claims claims = Jwts.parser().verifyWith(secretKey).build().parseSignedClaims(token).getPayload();
            Number uid = claims.get("uid", Number.class);
            Object rolesClaim = claims.get("roles");
            Set<String> roles = new LinkedHashSet<>();
            if (rolesClaim instanceof Collection<?> collection) {
                for (Object current : collection) {
                    if (current != null) {
                        roles.add(String.valueOf(current));
                    }
                }
            }

            return new JwtClaimsResponse(
                    claims.getIssuer(),
                    claims.getSubject(),
                    claims.getId(),
                    uid == null ? null : uid.longValue(),
                    roles,
                    claims.get("auth_source", String.class),
                    claims.get("provider_login", String.class),
                    toLocalDateTime(claims.getIssuedAt()),
                    toLocalDateTime(claims.getExpiration())
            );
        } catch (Exception ex) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid JWT token");
        }
    }

    private Date toDate(LocalDateTime value) {
        return Date.from(value.toInstant(ZoneOffset.UTC));
    }

    private LocalDateTime toLocalDateTime(Date value) {
        if (value == null) {
            return null;
        }
        Instant instant = value.toInstant();
        return LocalDateTime.ofInstant(instant, ZoneOffset.UTC);
    }
}
