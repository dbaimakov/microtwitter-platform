package org.ac.cst8277.baimakov.dmitriy.service;

import org.ac.cst8277.baimakov.dmitriy.client.UmsConnector;
import org.ac.cst8277.baimakov.dmitriy.dto.AuthContextResponse;
import org.ac.cst8277.baimakov.dmitriy.dto.ProducerIdsResponse;
import org.ac.cst8277.baimakov.dmitriy.dto.SubscriptionCreateRequest;
import org.ac.cst8277.baimakov.dmitriy.dto.SubscriptionResponse;
import org.ac.cst8277.baimakov.dmitriy.dto.SubscriptionUpdateRequest;
import org.ac.cst8277.baimakov.dmitriy.entity.SubscriptionEntity;
import org.ac.cst8277.baimakov.dmitriy.repository.SubscriptionRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

@Service
@Transactional(readOnly = true)
public class SubscriptionService {

    private static final Set<String> SUBSCRIPTION_ROLES = Set.of("SUBSCRIBER", "PRODUCER");
    private static final Set<String> VIEW_ROLES = Set.of("SUBSCRIBER", "PRODUCER", "ADMIN");
    private static final String PRODUCER_ROLE = "PRODUCER";

    private final SubscriptionRepository subscriptionRepository;
    private final AuthorizationService authorizationService;
    private final UmsConnector umsConnector;

    public SubscriptionService(
            SubscriptionRepository subscriptionRepository,
            AuthorizationService authorizationService,
            UmsConnector umsConnector
    ) {
        this.subscriptionRepository = subscriptionRepository;
        this.authorizationService = authorizationService;
        this.umsConnector = umsConnector;
    }

    @Transactional
    public SubscriptionResponse subscribe(String token, SubscriptionCreateRequest request) {
        AuthContextResponse auth = authorizationService.requireAnyRole(token, SUBSCRIPTION_ROLES);
        Long subscriberUserId = auth.userId();
        Long producerUserId = normalizeUserId(request.producerUserId(), "Producer user id is required");

        validateProducerTarget(subscriberUserId, producerUserId);

        if (subscriptionRepository.existsBySubscriberUserIdAndProducerUserId(subscriberUserId, producerUserId)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Subscriber already follows this producer");
        }

        SubscriptionEntity saved = subscriptionRepository.save(
                SubscriptionEntity.builder()
                        .subscriberUserId(subscriberUserId)
                        .producerUserId(producerUserId)
                        .build()
        );

        return toResponse(saved);
    }

    @Transactional
    public void unsubscribe(String token, Long producerUserId) {
        AuthContextResponse auth = authorizationService.requireAnyRole(token, SUBSCRIPTION_ROLES);

        SubscriptionEntity existing = subscriptionRepository
                .findBySubscriberUserIdAndProducerUserId(auth.userId(), producerUserId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Subscription not found"));

        subscriptionRepository.delete(existing);
    }

    public ProducerIdsResponse findProducerIdsForCurrentUser(String token) {
        AuthContextResponse auth = authorizationService.requireAnyRole(token, SUBSCRIPTION_ROLES);
        return new ProducerIdsResponse(auth.userId(), findProducerIds(auth.userId()));
    }

    public List<SubscriptionResponse> getSubscriptionsBySubscriber(String token, Long subscriberUserId) {
        AuthContextResponse auth = authorizationService.requireAnyRole(token, VIEW_ROLES);

        boolean sameUser = auth.userId().equals(subscriberUserId);
        boolean admin = auth.roles().contains("ADMIN");

        if (!sameUser && !admin) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Token does not match subscriber");
        }

        return subscriptionRepository.findBySubscriberUserIdOrderByCreatedAtDesc(subscriberUserId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public SubscriptionResponse replaceSubscription(String token, SubscriptionUpdateRequest request) {
        AuthContextResponse auth = authorizationService.requireAnyRole(token, SUBSCRIPTION_ROLES);
        Long subscriberUserId = auth.userId();
        Long oldProducerUserId = normalizeUserId(request.oldProducerUserId(), "Old producer user id is required");
        Long newProducerUserId = normalizeUserId(request.newProducerUserId(), "New producer user id is required");

        if (oldProducerUserId.equals(newProducerUserId)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Old and new producer ids must be different");
        }

        SubscriptionEntity existing = subscriptionRepository
                .findBySubscriberUserIdAndProducerUserId(subscriberUserId, oldProducerUserId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Subscription not found"));

        validateProducerTarget(subscriberUserId, newProducerUserId);

        if (subscriptionRepository.existsBySubscriberUserIdAndProducerUserId(subscriberUserId, newProducerUserId)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Subscriber already follows this producer");
        }

        existing.setProducerUserId(newProducerUserId);
        return toResponse(subscriptionRepository.save(existing));
    }

    public ProducerIdsResponse getProducerIdsBySubscriber(Long subscriberUserId) {
        return new ProducerIdsResponse(subscriberUserId, findProducerIds(subscriberUserId));
    }

    private void validateProducerTarget(Long subscriberUserId, Long producerUserId) {
        if (subscriberUserId.equals(producerUserId)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Subscriber cannot subscribe to themselves");
        }

        AuthContextResponse producer = umsConnector.getUserById(producerUserId);
        if (producer.roles() == null || !producer.roles().contains(PRODUCER_ROLE)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Target user is not a producer");
        }
    }

    private Long normalizeUserId(Long value, String message) {
        if (value == null || value < 1) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, message);
        }
        return value;
    }

    private List<Long> findProducerIds(Long subscriberUserId) {
        return List.copyOf(new LinkedHashSet<>(
                subscriptionRepository.findBySubscriberUserIdOrderByCreatedAtDesc(subscriberUserId)
                        .stream()
                        .map(SubscriptionEntity::getProducerUserId)
                        .toList()
        ));
    }

    private SubscriptionResponse toResponse(SubscriptionEntity entity) {
        return new SubscriptionResponse(
                entity.getSubscriptionId(),
                entity.getSubscriberUserId(),
                entity.getProducerUserId(),
                entity.getCreatedAt()
        );
    }
}
