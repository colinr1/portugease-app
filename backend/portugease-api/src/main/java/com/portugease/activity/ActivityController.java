package com.portugease.activity;

import com.portugease.activity.dto.ActivityAttemptSubmissionRequest;
import com.portugease.activity.dto.ActivityAttemptSubmissionResponse;
import com.portugease.activity.dto.ActivityContentResponse;
import com.portugease.activity.dto.ActivityResponse;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/activities")
public class ActivityController {

    private final ActivityContentService activityContentService;
    private final ActivityService activityService;
    private final ActivityAttemptService activityAttemptService;

    public ActivityController(
            ActivityContentService activityContentService,
            ActivityService activityService,
            ActivityAttemptService activityAttemptService
    ) {
        this.activityContentService = activityContentService;
        this.activityService = activityService;
        this.activityAttemptService = activityAttemptService;
    }

    @GetMapping("/{activityId}")
    public ActivityContentResponse getActivity(
            @PathVariable UUID activityId,
            @RequestParam(required = false) UUID userId
    ) {
        return activityContentService.getActivity(activityId, userId);
    }

    @GetMapping("/by-key/{activityKey}")
    public ActivityResponse getActivityByKey(@PathVariable String activityKey) {
        return activityService.getActivityByKey(activityKey);
    }

    @GetMapping
    public List<ActivityResponse> getActivitiesForLocation(@RequestParam String locationSlug) {
        return activityService.getActivitiesForLocation(locationSlug);
    }

    @PostMapping("/{activityId}/attempts")
    public ActivityAttemptSubmissionResponse submitAttempt(
            @PathVariable UUID activityId,
            @Valid @RequestBody ActivityAttemptSubmissionRequest request
    ) {
        return activityAttemptService.submitAttempt(activityId, request);
    }
}
