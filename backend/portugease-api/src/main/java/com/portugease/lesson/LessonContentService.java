package com.portugease.lesson;

import com.portugease.activity.Activity;
import com.portugease.activity.ActivityRepository;
import com.portugease.activity.AdaptiveDifficultyService;
import com.portugease.activity.dto.ActivityContentResponse;
import com.portugease.asset.StaticAsset;
import com.portugease.asset.dto.AssetMetadataResponse;
import com.portugease.common.exception.ResourceNotFoundException;
import com.portugease.hotspot.HotspotMapper;
import com.portugease.hotspot.dto.HotspotResponse;
import com.portugease.lesson.dto.IntroDialogueFocusMarkerResponse;
import com.portugease.lesson.dto.IntroDialogueLineResponse;
import com.portugease.lesson.dto.IntroDialogueResponse;
import com.portugease.lesson.dto.LessonDetailResponse;
import com.portugease.location.Location;
import com.portugease.location.LocationRepository;
import com.portugease.progress.LearnerLocationProgressRepository;
import com.portugease.user.DemoUserService;
import com.portugease.user.User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collections;
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

    public LessonContentService(
            LocationRepository locationRepository,
            ActivityRepository activityRepository,
            HotspotMapper hotspotMapper,
            DemoUserService demoUserService,
            LearnerLocationProgressRepository learnerLocationProgressRepository, AdaptiveDifficultyService adaptiveDifficultyService
    ) {
        this.locationRepository = locationRepository;
        this.activityRepository = activityRepository;
        this.hotspotMapper = hotspotMapper;
        this.demoUserService = demoUserService;
        this.learnerLocationProgressRepository = learnerLocationProgressRepository;
        this.adaptiveDifficultyService = adaptiveDifficultyService;
    }

    @Transactional(readOnly = true)
    public LessonDetailResponse getLesson(UUID lessonId) {
        Location location = locationRepository.findById(lessonId)
                .orElseThrow(() -> new ResourceNotFoundException("Lesson not found: " + lessonId));

        User demoUser = demoUserService.getDemoUser();

        List<Activity> activities = activityRepository
                .findAllByLocationIdAndActiveTrueOrderByDisplayOrderAsc(location.getId());

        List<HotspotResponse> hotspots = hotspotMapper.mapHotspots(
                location.getContentJson(),
                activities
        );

        List<ActivityContentResponse> activityResponses = activities.stream()
                .map(activity -> toActivityContent(activity, demoUser))
                .toList();

        IntroDialogueResponse introDialogue = extractIntroDialogue(location, demoUser);

        return new LessonDetailResponse(
                location.getId(),
                location.getId(),
                location.getCity().getId(),
                getLessonTitle(location),
                location.getSlug(),
                location.getDescription(),
                location.getEstimatedMinutes(),
                toAsset(location.getBackgroundAsset()),
                location.getContentJson(),
                hotspots,
                activityResponses,
                introDialogue
        );
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
                valueAsString(rawIntroDialogue.get("title")),
                valueAsString(rawIntroDialogue.get("description")),
                valueAsBoolean(rawIntroDialogue.get("autoOpenOnFirstVisit")),
                alreadySeen,
                valueAsString(rawIntroDialogue.get("hotspotId")),
                valueAsStringList(rawIntroDialogue.get("targetLearningItemKeys")),
                extractDialogueLines(rawIntroDialogue.get("lines"))
        );
    }

    private List<IntroDialogueLineResponse> extractDialogueLines(Object linesObject) {
        if (!(linesObject instanceof List<?> rawLines)) {
            return Collections.emptyList();
        }

        List<IntroDialogueLineResponse> lines = new ArrayList<>();

        for (Object lineObject : rawLines) {
            if (!(lineObject instanceof Map<?, ?> rawLine)) {
                continue;
            }

            lines.add(new IntroDialogueLineResponse(
                    valueAsString(rawLine.get("id")),
                    valueAsString(rawLine.get("speaker")),
                    valueAsString(rawLine.get("portugueseText")),
                    valueAsString(rawLine.get("englishTranslation")),
                    valueAsString(rawLine.get("audioPath")),
                    valueAsStringList(rawLine.get("targetLearningItemKeys")),
                    extractFocusMarkers(rawLine.get(("focusMarkers")))
            ));
        }

        return lines;
    }

    private List<IntroDialogueFocusMarkerResponse> extractFocusMarkers(Object focusMarkersObject) {
        if (!(focusMarkersObject instanceof List<?> rawFocusMarkers)) {
            return Collections.emptyList();
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
        if (value instanceof Number numberValue) {
            return numberValue.doubleValue();
        }

        if (value == null) {
            return null;
        }

        try {
            return Double.parseDouble(value.toString());
        } catch (NumberFormatException exception) {
            return null;
        }
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
        if (asset == null) {
            return null;
        }

        return new AssetMetadataResponse(
                asset.getId(),
                asset.getAssetKey(),
                asset.getAssetType(),
                asset.getFilePath(),
                asset.getAltText(),
                asset.getDescription(),
                asset.getMimeType()
        );
    }

    private String valueAsString(Object value) {
        return value == null ? null : value.toString();
    }

    private Boolean valueAsBoolean(Object value) {
        if (value instanceof Boolean booleanValue) {
            return booleanValue;
        }

        if (value == null) {
            return false;
        }

        return Boolean.parseBoolean(value.toString());
    }

    private List<String> valueAsStringList(Object value) {
        if (!(value instanceof List<?> rawList)) {
            return Collections.emptyList();
        }

        return rawList.stream()
                .filter(item -> item != null)
                .map(Object::toString)
                .toList();
    }
}