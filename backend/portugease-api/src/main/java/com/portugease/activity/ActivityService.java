package com.portugease.activity;

import com.portugease.activity.dto.ActivityResponse;
import com.portugease.common.exception.ResourceNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ActivityService {

    private final ActivityRepository activityRepository;
    private final ActivityDefinitionSanitizer activityDefinitionSanitizer;

    public ActivityService(
            ActivityRepository activityRepository,
            ActivityDefinitionSanitizer activityDefinitionSanitizer
    ) {
        this.activityRepository = activityRepository;
        this.activityDefinitionSanitizer = activityDefinitionSanitizer;
    }

    @Transactional(readOnly = true)
    public ActivityResponse getActivityByKey(String activityKey) {
        Activity activity = activityRepository.findByActivityKey(activityKey)
                .orElseThrow(() -> new ResourceNotFoundException("Activity not found: " + activityKey));

        return toResponse(activity);
    }

    @Transactional(readOnly = true)
    public List<ActivityResponse> getActivitiesForLocation(String locationSlug) {
        return activityRepository.findAllByLocation_SlugAndActiveTrueOrderByDisplayOrderAsc(locationSlug)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    private ActivityResponse toResponse(Activity activity) {
        return new ActivityResponse(
                activity.getId(),
                activity.getActivityKey(),
                activity.getActivityType(),
                activity.getTitle(),
                activity.getInstructions(),
                activityDefinitionSanitizer.sanitize(activity.getActivityType(), activity.getNormalDefinitionJson()),
                activity.getDisplayOrder()
        );
    }
}
