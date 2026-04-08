package org.ac.cst8277.baimakov.dmitriy.service;

import org.ac.cst8277.baimakov.dmitriy.dto.AuthContextResponse;
import org.ac.cst8277.baimakov.dmitriy.dto.MessageCreateRequest;
import org.ac.cst8277.baimakov.dmitriy.dto.MessageResponse;
import org.ac.cst8277.baimakov.dmitriy.entity.MessageEntity;
import org.ac.cst8277.baimakov.dmitriy.repository.MessageRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

@Service
@Transactional(readOnly = true)
public class MessageService {

    private static final Set<String> VIEWER_ROLES = Set.of("SUBSCRIBER", "PRODUCER", "ADMIN");
    private static final Set<String> PRODUCER_ONLY = Set.of("PRODUCER");
    private static final Set<String> DELETE_ROLES = Set.of("PRODUCER", "ADMIN");

    private final MessageRepository messageRepository;
    private final AuthorizationService authorizationService;

    public MessageService(MessageRepository messageRepository, AuthorizationService authorizationService) {
        this.messageRepository = messageRepository;
        this.authorizationService = authorizationService;
    }

    @Transactional
    public MessageResponse publish(String token, MessageCreateRequest request) {
        AuthContextResponse auth = authorizationService.requireAnyRole(token, PRODUCER_ONLY);

        MessageEntity saved = messageRepository.saveAndFlush(
                MessageEntity.builder()
                        .producerUserId(auth.userId())
                        .producerUsername(auth.username())
                        .content(request.content().trim())
                        .build()
        );

        MessageEntity refreshed = messageRepository.findById(saved.getMessageId()).orElse(saved);
        return toResponse(refreshed);
    }

    public List<MessageResponse> findAll(String token, Long producerUserId) {
        authorizationService.requireAuthenticated(token);

        List<MessageEntity> results = producerUserId == null
                ? messageRepository.findAllByOrderByCreatedAtDesc()
                : messageRepository.findByProducerUserIdOrderByCreatedAtDesc(producerUserId);

        return results.stream().map(this::toResponse).toList();
    }

    public List<MessageResponse> findByProducer(String token, Long producerUserId) {
        authorizationService.requireAuthenticated(token);

        return messageRepository.findByProducerUserIdOrderByCreatedAtDesc(producerUserId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    public List<MessageResponse> search(String token, String q) {
        authorizationService.requireAuthenticated(token);

        if (q == null || q.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Search query is required");
        }

        return messageRepository.searchByContent(q.trim())
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public void delete(String token, Long messageId) {
        AuthContextResponse auth = authorizationService.requireAnyRole(token, DELETE_ROLES);

        MessageEntity existing = messageRepository.findById(messageId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Message not found"));

        boolean admin = auth.roles().contains("ADMIN");
        boolean owner = auth.userId().equals(existing.getProducerUserId());

        if (!admin && !owner) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Insufficient role");
        }

        messageRepository.delete(existing);
    }

    public List<MessageResponse> findByProducerIds(List<Long> producerUserIds) {
        if (producerUserIds == null || producerUserIds.isEmpty()) {
            return List.of();
        }

        List<Long> normalized = List.copyOf(new LinkedHashSet<>(producerUserIds));

        return messageRepository.findByProducerUserIdInOrderByCreatedAtDesc(normalized)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    private MessageResponse toResponse(MessageEntity entity) {
        return new MessageResponse(
                entity.getMessageId(),
                entity.getProducerUserId(),
                entity.getProducerUsername(),
                entity.getContent(),
                entity.getCreatedAt()
        );
    }
}
