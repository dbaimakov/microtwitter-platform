package org.ac.cst8277.baimakov.dmitriy.dto;

import java.util.Set;

public record AuthContextResponse(Long userId, String username, Set<String> roles, boolean active) {
    public boolean hasAnyRole(Set<String> expected) {
        return roles.stream().anyMatch(expected::contains);
    }
}
