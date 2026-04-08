package org.ac.cst8277.baimakov.dmitriy.dto;

import javax.validation.constraints.NotNull;

public record SubscriptionCreateRequest(@NotNull Long producerUserId) {
}
