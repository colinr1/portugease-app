package com.portugease.adaptive;

import com.portugease.adaptive.dto.AdaptiveEventResponse;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/adaptive-events")
public class AdaptiveController {

    private final AdaptiveService adaptiveService;

    public AdaptiveController(AdaptiveService adaptiveService) {
        this.adaptiveService = adaptiveService;
    }

    @GetMapping
    public List<AdaptiveEventResponse> getRecentEvents() {
        return adaptiveService.getRecentEvents();
    }
}