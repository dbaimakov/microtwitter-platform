package org.ac.cst8277.baimakov.dmitriy.service;

import org.ac.cst8277.baimakov.dmitriy.client.UmsConnector;
import org.ac.cst8277.baimakov.dmitriy.dto.AuthContextResponse;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.Set;

@Service
public class AuthorizationService {

    private final UmsConnector umsConnector;

    public AuthorizationService(UmsConnector umsConnector) {
        this.umsConnector = umsConnector;
    }

    public AuthContextResponse requireAuthenticated(String token) {
        return umsConnector.validateToken(token);
    }

    public AuthContextResponse requireAnyRole(String token, Set<String> roles) {
        AuthContextResponse auth = requireAuthenticated(token);
        if (!auth.hasAnyRole(roles)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Insufficient role");
        }
        return auth;
    }
}
