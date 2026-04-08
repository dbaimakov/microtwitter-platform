package org.ac.cst8277.baimakov.dmitriy.dto;

import java.time.LocalDateTime;

public record MessageResponse(
        Long messageId,
        Long producerUserId,
        String producerUsername,
        String content,
        LocalDateTime createdAt
) {
}
