package org.ac.cst8277.baimakov.dmitriy.controller;

import org.ac.cst8277.baimakov.dmitriy.dto.ApiResponse;
import org.ac.cst8277.baimakov.dmitriy.dto.MessageCreateRequest;
import org.ac.cst8277.baimakov.dmitriy.dto.MessageResponse;
import org.ac.cst8277.baimakov.dmitriy.service.MessageService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/v1/messages")
public class MessageController {

    private final MessageService messageService;

    public MessageController(MessageService messageService) {
        this.messageService = messageService;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<MessageResponse>> publish(
            @RequestHeader("X-Auth-Token") String token,
            @Valid @RequestBody MessageCreateRequest request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created(
                        messageService.publish(token, request),
                        "Message has been created successfully"
                ));
    }

    @GetMapping
    public ApiResponse<List<MessageResponse>> listMessages(
            @RequestHeader("X-Auth-Token") String token,
            @RequestParam(required = false) Long producerUserId
    ) {
        return ApiResponse.ok(
                messageService.findAll(token, producerUserId),
                "List of messages has been requested successfully"
        );
    }

    @GetMapping("/producer/{producerUserId}")
    public ApiResponse<List<MessageResponse>> listByProducerAlias(
            @RequestHeader("X-Auth-Token") String token,
            @PathVariable Long producerUserId
    ) {
        return ApiResponse.ok(
                messageService.findByProducer(token, producerUserId),
                "List of messages has been requested successfully"
        );
    }

    @GetMapping("/search")
    public ApiResponse<List<MessageResponse>> searchMessages(
            @RequestHeader("X-Auth-Token") String token,
            @RequestParam String q
    ) {
        return ApiResponse.ok(
                messageService.search(token, q),
                "List of messages has been requested successfully"
        );
    }

    @DeleteMapping("/{messageId}")
    public ApiResponse<List<Object>> deleteMessage(
            @RequestHeader("X-Auth-Token") String token,
            @PathVariable Long messageId
    ) {
        messageService.delete(token, messageId);
        return ApiResponse.ok(List.of(), "Message has been deleted successfully");
    }
}
