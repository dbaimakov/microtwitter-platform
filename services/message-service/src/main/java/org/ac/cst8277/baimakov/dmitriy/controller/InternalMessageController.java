package org.ac.cst8277.baimakov.dmitriy.controller;

import org.ac.cst8277.baimakov.dmitriy.dto.ApiResponse;
import org.ac.cst8277.baimakov.dmitriy.dto.MessageResponse;
import org.ac.cst8277.baimakov.dmitriy.dto.ProducerIdsRequest;
import org.ac.cst8277.baimakov.dmitriy.service.MessageService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/internal/messages")
public class InternalMessageController {

    private final MessageService messageService;

    public InternalMessageController(MessageService messageService) {
        this.messageService = messageService;
    }

    @PostMapping("/by-producers")
    public ApiResponse<List<MessageResponse>> findByProducerIds(@RequestBody ProducerIdsRequest request) {
        return ApiResponse.ok(
                messageService.findByProducerIds(request.producerUserIds()),
                "List of messages requested successfully"
        );
    }
}
