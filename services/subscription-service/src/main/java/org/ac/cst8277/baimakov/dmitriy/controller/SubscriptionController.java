package org.ac.cst8277.baimakov.dmitriy.controller;

import org.ac.cst8277.baimakov.dmitriy.dto.ApiResponse;
import org.ac.cst8277.baimakov.dmitriy.dto.ProducerIdsResponse;
import org.ac.cst8277.baimakov.dmitriy.dto.SubscriptionCreateRequest;
import org.ac.cst8277.baimakov.dmitriy.dto.SubscriptionResponse;
import org.ac.cst8277.baimakov.dmitriy.dto.SubscriptionUpdateRequest;
import org.ac.cst8277.baimakov.dmitriy.service.SubscriptionService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/v1/subscriptions")
public class SubscriptionController {

    private final SubscriptionService subscriptionService;

    public SubscriptionController(SubscriptionService subscriptionService) {
        this.subscriptionService = subscriptionService;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<SubscriptionResponse>> subscribe(
            @RequestHeader("X-Auth-Token") String token,
            @Valid @RequestBody SubscriptionCreateRequest request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created(
                        subscriptionService.subscribe(token, request),
                        "Subscription has been created successfully"
                ));
    }

    @DeleteMapping("/{producerUserId}")
    public ApiResponse<List<Object>> unsubscribe(
            @RequestHeader("X-Auth-Token") String token,
            @PathVariable Long producerUserId
    ) {
        subscriptionService.unsubscribe(token, producerUserId);
        return ApiResponse.ok(List.of(), "Subscription has been deleted successfully");
    }

    @GetMapping("/producers")
    public ApiResponse<ProducerIdsResponse> getProducerIdsForCurrentUser(
            @RequestHeader("X-Auth-Token") String token
    ) {
        return ApiResponse.ok(
                subscriptionService.findProducerIdsForCurrentUser(token),
                "Producer ids have been requested successfully"
        );
    }

    @GetMapping("/subscriber/{subscriberUserId}")
    public ApiResponse<List<SubscriptionResponse>> getSubscriptionsBySubscriber(
            @RequestHeader("X-Auth-Token") String token,
            @PathVariable Long subscriberUserId
    ) {
        return ApiResponse.ok(
                subscriptionService.getSubscriptionsBySubscriber(token, subscriberUserId),
                "Subscription list has been requested successfully"
        );
    }

    @PutMapping
    public ApiResponse<SubscriptionResponse> updateSubscription(
            @RequestHeader("X-Auth-Token") String token,
            @Valid @RequestBody SubscriptionUpdateRequest request
    ) {
        return ApiResponse.ok(
                subscriptionService.replaceSubscription(token, request),
                "Subscription has been updated successfully"
        );
    }
}
