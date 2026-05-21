package com.portugease.activity;

import com.portugease.activity.dto.ActivityContentResponse;
import com.portugease.common.enums.DifficultyLevel;
import com.portugease.common.exception.ResourceNotFoundException;
import com.portugease.user.DemoUserService;
import com.portugease.user.User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.UUID;

@Service
public class ActivityContentService {

    private final ActivityRepository activityRepository;
    private final DemoUserService demoUserService;
    private final AdaptiveDifficultyService adaptiveDifficultyService;

    public ActivityContentService(
            ActivityRepository activityRepository,
            DemoUserService demoUserService,
            AdaptiveDifficultyService adaptiveDifficultyService
    ) {
        this.activityRepository = activityRepository;
        this.demoUserService = demoUserService;
        this.adaptiveDifficultyService = adaptiveDifficultyService;
    }

    @Transactional(readOnly = true)
    public ActivityContentResponse getActivity(UUID activityId) {
        Activity activity = activityRepository.findById(activityId)
                .orElseThrow(() -> new ResourceNotFoundException("Activity not found: " + activityId));

        User user = demoUserService.getDemoUser();

        DifficultyLevel selectedDifficulty = adaptiveDifficultyService.getCurrentDifficulty(
                user,
                activity.getActivityType()
        );

        Map<String, Object> selectedDefinition = adaptiveDifficultyService.selectDefinition(
                activity,
                selectedDifficulty
        );

        return new ActivityContentResponse(
                activity.getId(),
                activity.getLocation().getId(),
                activity.getActivityKey(),
                activity.getActivityType(),
                activity.getTitle(),
                activity.getInstructions(),
                selectedDefinition,
                activity.getLearningItemsJson(),
                activity.getMaxScore(),
                activity.getRequiredForCompletion(),
                activity.getDisplayOrder(),
                selectedDifficulty.name()
        );
    }
}