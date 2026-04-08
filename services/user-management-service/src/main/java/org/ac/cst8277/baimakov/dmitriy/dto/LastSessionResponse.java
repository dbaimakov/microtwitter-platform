package org.ac.cst8277.baimakov.dmitriy.dto;

import java.time.LocalDateTime;

public record LastSessionResponse(LocalDateTime issuedAt, LocalDateTime logoutAt, boolean active) {
}
