package org.ac.cst8277.baimakov.dmitriy.dto;

import java.util.List;

public record ProducerIdsResponse(Long subscriberUserId, List<Long> producerUserIds) {
}
