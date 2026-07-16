package com.portugease.activity;

import com.portugease.activity.dto.ActivityContentResponse;
import com.portugease.common.enums.DifficultyLevel;
import com.portugease.common.exception.ResourceNotFoundException;
import com.portugease.user.DemoUserService;
import com.portugease.user.User;
import com.portugease.user.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.UUID;

@Service
public class ActivityContentService {

    private final ActivityRepository activityRepository;
    private final DemoUserService demoUserService;
    private final AdaptiveDifficultyService adaptiveDifficultyService;
    private final ActivityDefinitionSanitizer activityDefinitionSanitizer;
    private final UserRepository userRepository;

    public ActivityContentService(
            ActivityRepository activityRepository,
            DemoUserService demoUserService,
            AdaptiveDifficultyService adaptiveDifficultyService,
            ActivityDefinitionSanitizer activityDefinitionSanitizer,
            UserRepository userRepository
    ) {
        this.activityRepository = activityRepository;
        this.demoUserService = demoUserService;
        this.adaptiveDifficultyService = adaptiveDifficultyService;
        this.activityDefinitionSanitizer = activityDefinitionSanitizer;
        this.userRepository = userRepository;
    }

    @Transactional(readOnly = true)
    public ActivityContentResponse getActivity(UUID activityId, UUID userId) {
        Activity activity = activityRepository.findById(activityId)
                .orElseThrow(() -> new ResourceNotFoundException("Activity not found: " + activityId));

        User user = resolveUser(userId);

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
                activity.getActivityKey(),
                activity.getActivityType(),
                activity.getTitle(),
                activity.getInstructions(),
                activityDefinitionSanitizer.sanitize(activity.getActivityType(), selectedDefinition),
                activity.getDisplayOrder(),
                selectedDifficulty.name()
        );
    }

    private User resolveUser(UUID userId) {
        if (userId == null) {
            return demoUserService.getDemoUser();
        }

        return userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + userId));
    }
}
