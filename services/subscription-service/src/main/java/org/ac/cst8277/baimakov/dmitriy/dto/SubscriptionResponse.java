package org.ac.cst8277.baimakov.dmitriy.dto;

import java.time.LocalDateTime;

public record SubscriptionResponse(
        Long subscriptionId,
        Long subscriberUserId,
        Long producerUserId,
        LocalDateTime createdAt
) {
}
