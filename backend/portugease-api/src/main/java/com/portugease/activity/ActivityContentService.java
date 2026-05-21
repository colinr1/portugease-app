package com.portugease.activity;

import com.portugease.activity.dto.ActivityContentResponse;
import com.portugease.common.exception.ResourceNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class ActivityContentService {

    private final ActivityRepository activityRepository;

    public ActivityContentService(ActivityRepository activityRepository) {
        this.activityRepository = activityRepository;
    }

    @Transactional(readOnly = true)
    public ActivityContentResponse getActivity(UUID activityId) {
        Activity activity = activityRepository.findById(activityId)
                .orElseThrow(() -> new ResourceNotFoundException("Activity not found: " + activityId));

        return new ActivityContentResponse(
                activity.getId(),
                activity.getLocation().getId(),
                activity.getActivityKey(),
                activity.getActivityType(),
                activity.getTitle(),
                activity.getInstructions(),
                activity.getDefinitionJson(),
                activity.getLearningItemsJson(),
                activity.getMaxScore(),
                activity.getRequiredForCompletion(),
                activity.getDisplayOrder()
        );
    }
}