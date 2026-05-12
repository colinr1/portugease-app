package com.portugease.location;

import com.portugease.hotspot.dto.HotspotResponse;
import com.portugease.lesson.dto.LessonSummaryResponse;
import com.portugease.location.dto.IntroDialogueSeenRequest;
import com.portugease.location.dto.IntroDialogueSeenResponse;
import com.portugease.location.dto.LocationDetailResponse;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/locations")
public class LocationController {

    private final LocationContentService locationContentService;

    public LocationController(LocationContentService locationContentService) {
        this.locationContentService = locationContentService;
    }

    @GetMapping("/{locationId}")
    public LocationDetailResponse getLocation(@PathVariable UUID locationId) {
        return locationContentService.getLocation(locationId);
    }

    @GetMapping("/{locationId}/lessons")
    public List<LessonSummaryResponse> getLocationLessons(@PathVariable UUID locationId) {
        return locationContentService.getLessonsForLocation(locationId);
    }

    @GetMapping("/{locationId}/hotspots")
    public List<HotspotResponse> getLocationHotspots(@PathVariable UUID locationId) {
        return locationContentService.getHotspotsForLocation(locationId);
    }

    @PostMapping("/{locationId}/intro-dialogue/seen")
    public IntroDialogueSeenResponse markIntroDialogueSeen(
            @PathVariable UUID locationId,
            @RequestBody(required = false) IntroDialogueSeenRequest request
    ) {
        return locationContentService.markIntroDialogueSeen(locationId, request);
    }
}