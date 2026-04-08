package org.ac.cst8277.baimakov.dmitriy.controller;

import org.ac.cst8277.baimakov.dmitriy.dto.ApiResponse;
import org.ac.cst8277.baimakov.dmitriy.dto.MessageResponse;
import org.ac.cst8277.baimakov.dmitriy.service.TimelineService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class TimelineController {

    private final TimelineService timelineService;

    public TimelineController(TimelineService timelineService) {
        this.timelineService = timelineService;
    }

    @GetMapping("/api/v1/timeline/subscriber/{subscriberUserId}")
    public ApiResponse<List<MessageResponse>> getFeedForSubscriber(
            @RequestHeader("X-Auth-Token") String token,
            @PathVariable Long subscriberUserId
    ) {
        return ApiResponse.ok(
                timelineService.getFeed(token, subscriberUserId),
                "List of messages has been requested successfully"
        );
    }

    @GetMapping("/api/v1/messages/subscriber/{subscriberUserId}")
    public ApiResponse<List<MessageResponse>> getFeedAlias(
            @RequestHeader("X-Auth-Token") String token,
            @PathVariable Long subscriberUserId
    ) {
        return ApiResponse.ok(
                timelineService.getFeed(token, subscriberUserId),
                "List of messages has been requested successfully"
        );
    }
}
