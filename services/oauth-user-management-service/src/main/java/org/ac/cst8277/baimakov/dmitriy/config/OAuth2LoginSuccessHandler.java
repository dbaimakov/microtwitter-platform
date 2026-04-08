package org.ac.cst8277.baimakov.dmitriy.config;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.ac.cst8277.baimakov.dmitriy.dto.ApiResponse;
import org.ac.cst8277.baimakov.dmitriy.dto.LoginResponse;
import org.ac.cst8277.baimakov.dmitriy.service.AuthService;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.web.server.WebFilterExchange;
import org.springframework.security.web.server.authentication.ServerAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.nio.charset.StandardCharsets;

@Component
public class OAuth2LoginSuccessHandler implements ServerAuthenticationSuccessHandler {

    private final AuthService authService;
    private final ObjectMapper objectMapper;

    public OAuth2LoginSuccessHandler(AuthService authService, ObjectMapper objectMapper) {
        this.authService = authService;
        this.objectMapper = objectMapper;
    }

    @Override
    public Mono<Void> onAuthenticationSuccess(WebFilterExchange webFilterExchange, Authentication authentication) {
        if (!(authentication instanceof OAuth2AuthenticationToken token)) {
            return writeError(webFilterExchange, HttpStatus.INTERNAL_SERVER_ERROR, "Unsupported authentication type");
        }

        return Mono.fromCallable(() -> authService.loginWithGithub(token.getPrincipal().getAttributes()))
                .subscribeOn(Schedulers.boundedElastic())
                .flatMap(loginResponse -> writeSuccess(webFilterExchange, loginResponse))
                .onErrorResume(ResponseStatusException.class,
                        ex -> writeError(webFilterExchange, ex.getStatus(), ex.getReason() == null ? "OAuth login failed" : ex.getReason()))
                .onErrorResume(ex -> writeError(webFilterExchange, HttpStatus.INTERNAL_SERVER_ERROR, "OAuth login failed"));
    }

    private Mono<Void> writeSuccess(WebFilterExchange exchange, LoginResponse loginResponse) {
        return writeJson(exchange, HttpStatus.OK, ApiResponse.ok(loginResponse, "GitHub login completed successfully"));
    }

    private Mono<Void> writeError(WebFilterExchange exchange, HttpStatus status, String message) {
        return writeJson(exchange, status, ApiResponse.error(String.valueOf(status.value()), java.util.List.of(), message));
    }

    private Mono<Void> writeJson(WebFilterExchange exchange, HttpStatus status, Object payload) {
        byte[] bytes;
        try {
            bytes = objectMapper.writeValueAsString(payload).getBytes(StandardCharsets.UTF_8);
        } catch (JsonProcessingException ex) {
            bytes = ("{\"code\":\"500\",\"data\":[],\"message\":\"Serialization failure\"}").getBytes(StandardCharsets.UTF_8);
            status = HttpStatus.INTERNAL_SERVER_ERROR;
        }

        exchange.getExchange().getResponse().setStatusCode(status);
        exchange.getExchange().getResponse().getHeaders().setContentType(MediaType.APPLICATION_JSON);
        return exchange.getExchange().getResponse().writeWith(Mono.just(exchange.getExchange().getResponse().bufferFactory().wrap(bytes)));
    }
}
