package org.ac.cst8277.baimakov.dmitriy.service;
import org.ac.cst8277.baimakov.dmitriy.client.MessageConnector;
import org.ac.cst8277.baimakov.dmitriy.client.SubscriptionConnector;
import org.ac.cst8277.baimakov.dmitriy.dto.AuthContextResponse;
import org.ac.cst8277.baimakov.dmitriy.dto.MessageResponse;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import java.util.List;
import java.util.Set;
@Service
public class TimelineService {
    private static final Set<String> VIEWER_ROLES = Set.of("SUBSCRIBER", "PRODUCER", "ADMIN");
    private final AuthorizationService authorizationService;
    private final SubscriptionConnector subscriptionConnector;
    private final MessageConnector messageConnector;
    public TimelineService(
            AuthorizationService authorizationService,
            SubscriptionConnector subscriptionConnector,
            MessageConnector messageConnector
    ) {
        this.authorizationService = authorizationService;
        this.subscriptionConnector = subscriptionConnector;
        this.messageConnector = messageConnector;
    }
    public List<MessageResponse> getFeed(String token, Long subscriberUserId) {
        AuthContextResponse auth = authorizationService.requireAnyRole(token, VIEWER_ROLES);
        boolean sameUser = auth.userId().equals(subscriberUserId);
        boolean admin = auth.roles().contains("ADMIN");

        if (!sameUser && !admin) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Token does not matchy matchy");
        }
        List<Long> producerIds = subscriptionConnector.findProducerIds(subscriberUserId);
        if (producerIds.isEmpty()) {
            return List.of();
        }
        return messageConnector.findByProducerIds(producerIds);
    }
}
