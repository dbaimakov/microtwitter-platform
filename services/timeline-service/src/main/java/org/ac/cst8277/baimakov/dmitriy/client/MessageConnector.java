package org.ac.cst8277.baimakov.dmitriy.client;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.ac.cst8277.baimakov.dmitriy.dto.MessageResponse;
import org.ac.cst8277.baimakov.dmitriy.dto.ProducerIdsRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import org.springframework.web.server.ResponseStatusException;

import java.time.Duration;
import java.util.LinkedHashSet;
import java.util.List;

@Component
public class MessageConnector {

    private static final Duration REQUEST_TIMEOUT = Duration.ofSeconds(5);

    private final ObjectMapper objectMapper;
    private final WebClient webClient;

    public MessageConnector(
            ObjectMapper objectMapper,
            WebClient.Builder webClientBuilder,
            @Value("${clients.message.base-url}") String baseUrl
    ) {
        this.objectMapper = objectMapper;
        this.webClient = webClientBuilder.baseUrl(baseUrl).build();
    }

    public List<MessageResponse> findByProducerIds(List<Long> producerUserIds) {
        if (producerUserIds == null || producerUserIds.isEmpty()) {
            return List.of();
        }

        List<Long> normalizedIds = List.copyOf(new LinkedHashSet<>(producerUserIds));

        try {
            JsonNode root = webClient.post()
                    .uri("/api/v1/internal/messages/by-producers")
                    .contentType(MediaType.APPLICATION_JSON)
                    .accept(MediaType.APPLICATION_JSON)
                    .bodyValue(new ProducerIdsRequest(normalizedIds))
                    .retrieve()
                    .bodyToMono(JsonNode.class)
                    .block(REQUEST_TIMEOUT);

            JsonNode data = root == null ? null : root.get("data");
            if (data == null || !data.isArray()) {
                return List.of();
            }

            return objectMapper.readValue(
                    objectMapper.writeValueAsBytes(data),
                    new TypeReference<List<MessageResponse>>() {
                    }
            );
        } catch (WebClientResponseException ex) {
            throw toResponseStatusException(ex, HttpStatus.BAD_GATEWAY, "Message service error");
        } catch (ResponseStatusException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to communicate with Message Service", ex);
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
