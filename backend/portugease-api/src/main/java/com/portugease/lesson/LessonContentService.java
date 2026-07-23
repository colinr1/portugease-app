package com.portugease.lesson;

import com.portugease.activity.Activity;
import com.portugease.activity.ActivityDefinitionSanitizer;
import com.portugease.activity.ActivityRepository;
import com.portugease.activity.AdaptiveDifficultyService;
import com.portugease.activity.dto.ActivityContentResponse;
import com.portugease.asset.AssetResponseMapper;
import com.portugease.asset.StaticAsset;
import com.portugease.asset.dto.AssetMetadataResponse;
import com.portugease.common.exception.ResourceNotFoundException;
import com.portugease.common.json.JsonValueReader;
import com.portugease.hotspot.HotspotMapper;
import com.portugease.hotspot.dto.HotspotResponse;
import com.portugease.lesson.dto.IntroDialogueFocusMarkerResponse;
import com.portugease.lesson.dto.IntroDialogueLineResponse;
import com.portugease.lesson.dto.IntroDialogueResponse;
import com.portugease.lesson.dto.LessonDetailResponse;
import com.portugease.location.Location;
import com.portugease.location.LocationRepository;
import com.portugease.progress.LearnerLocationProgressRepository;
import com.portugease.progress.ProgressionService;
import com.portugease.user.DemoUserService;
import com.portugease.user.User;
import com.portugease.user.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class LessonContentService {

    private final LocationRepository locationRepository;
    private final ActivityRepository activityRepository;
    private final HotspotMapper hotspotMapper;
    private final DemoUserService demoUserService;
    private final LearnerLocationProgressRepository learnerLocationProgressRepository;
    private final AdaptiveDifficultyService adaptiveDifficultyService;
    private final ActivityDefinitionSanitizer activityDefinitionSanitizer;
    private final UserRepository userRepository;
    private final ProgressionService progressionService;

    public LessonContentService(
            LocationRepository locationRepository,
            ActivityRepository activityRepository,
            HotspotMapper hotspotMapper,
            DemoUserService demoUserService,
            LearnerLocationProgressRepository learnerLocationProgressRepository,
            AdaptiveDifficultyService adaptiveDifficultyService,
            ActivityDefinitionSanitizer activityDefinitionSanitizer,
            UserRepository userRepository,
            ProgressionService progressionService
    ) {
        this.locationRepository = locationRepository;
        this.activityRepository = activityRepository;
        this.hotspotMapper = hotspotMapper;
        this.demoUserService = demoUserService;
        this.learnerLocationProgressRepository = learnerLocationProgressRepository;
        this.adaptiveDifficultyService = adaptiveDifficultyService;
        this.activityDefinitionSanitizer = activityDefinitionSanitizer;
        this.userRepository = userRepository;
        this.progressionService = progressionService;
    }

    @Transactional
    public LessonDetailResponse getLesson(UUID lessonId, UUID userId) {
        Location location = locationRepository.findById(lessonId)
                .orElseThrow(() -> new ResourceNotFoundException("Lesson not found: " + lessonId));

        return getLesson(location, userId);
    }

    @Transactional
    public LessonDetailResponse getLessonByLocationSlug(String locationSlug, UUID userId) {
        Location location = locationRepository.findBySlug(locationSlug)
                .orElseThrow(() -> new ResourceNotFoundException("Lesson not found: " + locationSlug));

        return getLesson(location, userId);
    }

    private LessonDetailResponse getLesson(Location location, UUID userId) {
        User user = resolveUser(userId);
        progressionService.assertLocationUnlocked(user, location);

        List<Activity> activities = activityRepository
                .findAllByLocationIdAndActiveTrueOrderByDisplayOrderAsc(location.getId());

        List<HotspotResponse> hotspots = hotspotMapper.mapHotspots(
                location.getContentJson(),
                activities
        );

        List<ActivityContentResponse> activityResponses = activities.stream()
                .map(activity -> toActivityContent(activity, user))
                .toList();

        IntroDialogueResponse introDialogue = extractIntroDialogue(location, user);

        return new LessonDetailResponse(
                location.getId(),
                location.getId(),
                location.getCity().getId(),
                location.getCity().getSlug(),
                getLessonTitle(location),
                location.getSlug(),
                toAsset(location.getBackgroundAsset()),
                location.getContentJson(),
                hotspots,
                activityResponses,
                introDialogue
        );
    }

    private User resolveUser(UUID userId) {
        if (userId == null) {
            return demoUserService.getDemoUser();
        }

        return userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + userId));
    }

    private ActivityContentResponse toActivityContent(Activity activity, User user) {
        var selectedDifficulty = adaptiveDifficultyService.getCurrentDifficulty(
                user,
                activity.getActivityType()
        );

        var selectedDefinition = adaptiveDifficultyService.selectDefinition(
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

    private IntroDialogueResponse extractIntroDialogue(Location location, User user) {
        Map<String, Object> contentJson = location.getContentJson();

        if (contentJson == null || contentJson.isEmpty()) {
            return null;
        }

        Object introDialogueObject = contentJson.get("introDialogue");

        if (!(introDialogueObject instanceof Map<?, ?> rawIntroDialogue)) {
            return null;
        }

        boolean alreadySeen = learnerLocationProgressRepository.findByUserAndLocation(user, location)
                .map(progress -> Boolean.TRUE.equals(progress.getIntroDialogueSeen()))
                .orElse(false);

        return new IntroDialogueResponse(
                valueAsString(rawIntroDialogue.get("id")),
                valueAsBoolean(rawIntroDialogue.get("autoOpenOnFirstVisit")),
                alreadySeen,
                extractDialogueLines(rawIntroDialogue.get("lines"))
        );
    }

    private List<IntroDialogueLineResponse> extractDialogueLines(Object linesObject) {
        if (!(linesObject instanceof List<?> rawLines)) {
            return List.of();
        }

        List<IntroDialogueLineResponse> lines = new ArrayList<>();

        for (Object lineObject : rawLines) {
            if (!(lineObject instanceof Map<?, ?> rawLine)) {
                continue;
            }

            lines.add(new IntroDialogueLineResponse(
                    valueAsString(rawLine.get("speaker")),
                    valueAsString(rawLine.get("portugueseText")),
                    valueAsString(rawLine.get("englishTranslation")),
                    valueAsString(rawLine.get("audioPath")),
                    extractFocusMarkers(rawLine.get(("focusMarkers")))
            ));
        }

        return lines;
    }

    private List<IntroDialogueFocusMarkerResponse> extractFocusMarkers(Object focusMarkersObject) {
        if (!(focusMarkersObject instanceof List<?> rawFocusMarkers)) {
            return List.of();
        }

        List<IntroDialogueFocusMarkerResponse> focusMarkers = new ArrayList<>();

        for (Object focusMarkerObject : rawFocusMarkers) {
            if (!(focusMarkerObject instanceof Map<?, ?> rawFocusMarker)) {
                continue;
            }

            focusMarkers.add(new IntroDialogueFocusMarkerResponse(
                    valueAsString(rawFocusMarker.get("id")),
                    valueAsDouble(rawFocusMarker.get("xPercent")),
                    valueAsDouble(rawFocusMarker.get("yPercent")),
                    valueAsString(rawFocusMarker.get("ariaLabel"))
            ));
        }

        return focusMarkers;
    }

    private Double valueAsDouble(Object value) {
        return JsonValueReader.asDouble(value);
    }

    private String getLessonTitle(Location location) {
        Map<String, Object> contentJson = location.getContentJson();

        if (contentJson == null) {
            return location.getName();
        }

        Object title = contentJson.get("title");
        return title == null ? location.getName() : title.toString();
    }

    private AssetMetadataResponse toAsset(StaticAsset asset) {
        return AssetResponseMapper.toMetadataResponse(asset);
    }

    private String valueAsString(Object value) {
        return JsonValueReader.asString(value);
    }

    private Boolean valueAsBoolean(Object value) {
        return JsonValueReader.asBoolean(value);
    }
}
