package org.ac.cst8277.baimakov.dmitriy.client;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.ac.cst8277.baimakov.dmitriy.dto.AuthContextResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import org.springframework.web.server.ResponseStatusException;

import java.time.Duration;

@Component
public class UmsConnector {

    private static final Duration REQUEST_TIMEOUT = Duration.ofSeconds(5);

    private final ObjectMapper objectMapper;
    private final WebClient webClient;

    public UmsConnector(
            ObjectMapper objectMapper,
            WebClient.Builder webClientBuilder,
            @Value("${clients.ums.base-url}") String baseUrl
    ) {
        this.objectMapper = objectMapper;
        this.webClient = webClientBuilder.baseUrl(baseUrl).build();
    }

    public AuthContextResponse validateToken(String token) {
        if (token == null || token.isBlank()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid token");
        }

        try {
            JsonNode root = webClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/api/v1/internal/auth/validate")
                            .queryParam("token", token.trim())
                            .build())
                    .accept(MediaType.APPLICATION_JSON)
                    .retrieve()
                    .bodyToMono(JsonNode.class)
                    .block(REQUEST_TIMEOUT);

            JsonNode data = root == null ? null : root.get("data");
            if (data == null || data.isNull()) {
                throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid token");
            }

            return objectMapper.treeToValue(data, AuthContextResponse.class);
        } catch (WebClientResponseException ex) {
            throw toResponseStatusException(ex, HttpStatus.UNAUTHORIZED, "UMS error");
        } catch (ResponseStatusException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to communicate with UMS", ex);
        }
    }

    public AuthContextResponse getUserById(Long userId) {
        if (userId == null || userId < 1) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "User id is required");
        }

        try {
            JsonNode root = webClient.get()
                    .uri("/api/v1/internal/users/{userId}", userId)
                    .accept(MediaType.APPLICATION_JSON)
                    .retrieve()
                    .bodyToMono(JsonNode.class)
                    .block(REQUEST_TIMEOUT);

            JsonNode data = root == null ? null : root.get("data");
            if (data == null || data.isNull()) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found");
            }

            return objectMapper.treeToValue(data, AuthContextResponse.class);
        } catch (WebClientResponseException ex) {
            throw toResponseStatusException(ex, HttpStatus.NOT_FOUND, "UMS error");
        } catch (ResponseStatusException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to communicate with UMS", ex);
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
