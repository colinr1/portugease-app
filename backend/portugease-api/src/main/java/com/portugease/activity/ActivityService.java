package com.portugease.activity;

import com.portugease.activity.dto.ActivityResponse;
import com.portugease.common.exception.ResourceNotFoundException;
import com.portugease.location.Location;
import com.portugease.location.LocationRepository;
import com.portugease.progress.ProgressionService;
import com.portugease.user.DemoUserService;
import com.portugease.user.User;
import com.portugease.user.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class ActivityService {

    private final ActivityRepository activityRepository;
    private final ActivityDefinitionSanitizer activityDefinitionSanitizer;
    private final LocationRepository locationRepository;
    private final DemoUserService demoUserService;
    private final UserRepository userRepository;
    private final ProgressionService progressionService;

    public ActivityService(
            ActivityRepository activityRepository,
            ActivityDefinitionSanitizer activityDefinitionSanitizer,
            LocationRepository locationRepository,
            DemoUserService demoUserService,
            UserRepository userRepository,
            ProgressionService progressionService
    ) {
        this.activityRepository = activityRepository;
        this.activityDefinitionSanitizer = activityDefinitionSanitizer;
        this.locationRepository = locationRepository;
        this.demoUserService = demoUserService;
        this.userRepository = userRepository;
        this.progressionService = progressionService;
    }

    @Transactional
    public ActivityResponse getActivityByKey(String activityKey, UUID userId) {
        Activity activity = activityRepository.findByActivityKey(activityKey)
                .orElseThrow(() -> new ResourceNotFoundException("Activity not found: " + activityKey));

        User user = resolveUser(userId);
        progressionService.assertLocationUnlocked(user, activity.getLocation());

        return toResponse(activity);
    }

    @Transactional
    public List<ActivityResponse> getActivitiesForLocation(String locationSlug, UUID userId) {
        Location location = locationRepository.findBySlug(locationSlug)
                .orElseThrow(() -> new ResourceNotFoundException("Location not found: " + locationSlug));
        User user = resolveUser(userId);
        progressionService.assertLocationUnlocked(user, location);

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

    private User resolveUser(UUID userId) {
        if (userId == null) {
            return demoUserService.getDemoUser();
        }

        return userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + userId));
    }
}
