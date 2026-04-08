package org.ac.cst8277.baimakov.dmitriy.controller;

import org.ac.cst8277.baimakov.dmitriy.dto.ApiResponse;
import org.ac.cst8277.baimakov.dmitriy.dto.ProducerIdsResponse;
import org.ac.cst8277.baimakov.dmitriy.service.SubscriptionService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/internal")
public class InternalSubscriptionController {

    private final SubscriptionService subscriptionService;

    public InternalSubscriptionController(SubscriptionService subscriptionService) {
        this.subscriptionService = subscriptionService;
    }

    @GetMapping("/subscriber/{subscriberUserId}/producer-ids")
    public ApiResponse<ProducerIdsResponse> getProducerIdsBySubscriber(@PathVariable Long subscriberUserId) {
        return ApiResponse.ok(
                subscriptionService.getProducerIdsBySubscriber(subscriberUserId),
                "Producer ids have been requested successfully"
        );
    }
}
