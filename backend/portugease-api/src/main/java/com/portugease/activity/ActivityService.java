package com.portugease.activity;

import com.portugease.activity.dto.ActivityResponse;
import com.portugease.common.exception.ResourceNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ActivityService {

    private final ActivityRepository activityRepository;

    public ActivityService(ActivityRepository activityRepository) {
        this.activityRepository = activityRepository;
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
                activity.getLocation().getId(),
                activity.getActivityKey(),
                activity.getActivityType(),
                activity.getTitle(),
                activity.getInstructions(),
                activity.getDefinitionJson(),
                activity.getLearningItemsJson(),
                activity.getMaxScore(),
                activity.getRequiredForCompletion()
        );
    }
}
