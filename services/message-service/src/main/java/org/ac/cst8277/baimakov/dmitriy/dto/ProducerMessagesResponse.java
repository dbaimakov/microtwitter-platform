package org.ac.cst8277.baimakov.dmitriy.dto;

import java.util.List;

public record ProducerMessagesResponse(Long producerUserId, List<MessageResponse> messages) {
}
