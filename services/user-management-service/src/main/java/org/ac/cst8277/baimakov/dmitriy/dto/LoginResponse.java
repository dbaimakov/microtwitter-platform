package org.ac.cst8277.baimakov.dmitriy.dto;

import java.util.Set;

public record LoginResponse(String token, Long userId, String username, Set<String> roles) {
}
