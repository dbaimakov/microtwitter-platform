package org.ac.cst8277.baimakov.dmitriy.client;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.ac.cst8277.baimakov.dmitriy.dto.ProducerIdsResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import org.springframework.web.server.ResponseStatusException;

import java.time.Duration;
import java.util.List;

@Component
public class SubscriptionConnector {

    private static final Duration REQUEST_TIMEOUT = Duration.ofSeconds(5);

    private final ObjectMapper objectMapper;
    private final WebClient webClient;

    public SubscriptionConnector(
            ObjectMapper objectMapper,
            WebClient.Builder webClientBuilder,
            @Value("${clients.subscription.base-url}") String baseUrl
    ) {
        this.objectMapper = objectMapper;
        this.webClient = webClientBuilder.baseUrl(baseUrl).build();
    }

    public List<Long> findProducerIds(Long subscriberUserId) {
        try {
            JsonNode root = webClient.get()
                    .uri("/api/v1/internal/subscriber/{subscriberUserId}/producer-ids", subscriberUserId)
                    .accept(MediaType.APPLICATION_JSON)
                    .retrieve()
                    .bodyToMono(JsonNode.class)
                    .block(REQUEST_TIMEOUT);

            JsonNode data = root == null ? null : root.get("data");
            if (data == null || data.isNull()) {
                return List.of();
            }

            ProducerIdsResponse response = objectMapper.treeToValue(data, ProducerIdsResponse.class);
            if (response.producerUserIds() == null) {
                return List.of();
            }

            return List.copyOf(response.producerUserIds());
        } catch (WebClientResponseException ex) {
            throw toResponseStatusException(ex, HttpStatus.BAD_GATEWAY, "Subscription service error");
        } catch (ResponseStatusException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to communicate with Subscription Service", ex);
        }
    }

    private ResponseStatusException toResponseStatusException(
            WebClientResponseException ex,
            HttpStatus fallbackStatus,
            String fallbackMessage
    ) {
        String message = fallbackMessage;

        try {
            JsonNode root = objectMapper.readTree(ex.getResponseBodyAsString());
            if (root.hasNonNull("message")) {
                message = root.get("message").asText();
            }
        } catch (Exception ignored) {
            // keep fallback message
        }

        HttpStatus status = HttpStatus.resolve(ex.getRawStatusCode());
        return new ResponseStatusException(status == null ? fallbackStatus : status, message, ex);
    }
}
