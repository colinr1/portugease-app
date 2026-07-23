package com.portugease.location;

import com.portugease.activity.Activity;
import com.portugease.activity.ActivityRepository;
import com.portugease.asset.AssetResponseMapper;
import com.portugease.asset.StaticAsset;
import com.portugease.asset.dto.AssetMetadataResponse;
import com.portugease.common.enums.LocationStatus;
import com.portugease.common.exception.ResourceNotFoundException;
import com.portugease.common.json.JsonValueReader;
import com.portugease.hotspot.HotspotMapper;
import com.portugease.hotspot.dto.HotspotResponse;
import com.portugease.lesson.dto.LessonSummaryResponse;
import com.portugease.location.dto.IntroDialogueSeenRequest;
import com.portugease.location.dto.IntroDialogueSeenResponse;
import com.portugease.location.dto.LocationDetailResponse;
import com.portugease.location.dto.LocationMenuItemResponse;
import com.portugease.progress.LearnerLocationProgress;
import com.portugease.progress.LearnerLocationProgressRepository;
import com.portugease.progress.ProgressionService;
import com.portugease.user.DemoUserService;
import com.portugease.user.User;
import com.portugease.user.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class LocationContentService {

    private final LocationRepository locationRepository;
    private final ActivityRepository activityRepository;
    private final LearnerLocationProgressRepository learnerLocationProgressRepository;
    private final DemoUserService demoUserService;
    private final UserRepository userRepository;
    private final HotspotMapper hotspotMapper;
    private final ProgressionService progressionService;

    public LocationContentService(
            LocationRepository locationRepository,
            ActivityRepository activityRepository,
            LearnerLocationProgressRepository learnerLocationProgressRepository,
            DemoUserService demoUserService,
            UserRepository userRepository,
            HotspotMapper hotspotMapper,
            ProgressionService progressionService
    ) {
        this.locationRepository = locationRepository;
        this.activityRepository = activityRepository;
        this.learnerLocationProgressRepository = learnerLocationProgressRepository;
        this.demoUserService = demoUserService;
        this.userRepository = userRepository;
        this.hotspotMapper = hotspotMapper;
        this.progressionService = progressionService;
    }

    @Transactional(readOnly = true)
    public List<LocationMenuItemResponse> getLocationMenuForCity(UUID cityId, UUID userId) {
        User user = resolveUser(userId);

        return locationRepository.findAllByCityIdAndActiveTrueOrderByDisplayOrderAsc(cityId)
                .stream()
                .map(location -> toMenuItem(location, getLocationStatus(user, location)))
                .toList();
    }

    @Transactional
    public LocationDetailResponse getLocation(UUID locationId, UUID userId) {
        User user = resolveUser(userId);

        Location location = findLocation(locationId);
        progressionService.assertLocationUnlocked(user, location);

        List<Activity> activities = activityRepository
                .findAllByLocationIdAndActiveTrueOrderByDisplayOrderAsc(locationId);

        List<HotspotResponse> hotspots = hotspotMapper.mapHotspots(
                location.getContentJson(),
                activities
        );

        return new LocationDetailResponse(
                location.getId(),
                location.getCity().getId(),
                location.getCity().getSlug(),
                location.getName(),
                location.getSlug(),
                location.getDisplayOrder(),
                getLocationStatus(user, location),
                toAsset(location.getBackgroundAsset()),
                location.getContentJson(),
                hotspots,
                getLessonsForLocation(locationId, user.getId())
        );
    }

    @Transactional
    public List<LessonSummaryResponse> getLessonsForLocation(UUID locationId, UUID userId) {
        Location location = findLocation(locationId);
        User user = resolveUser(userId);
        progressionService.assertLocationUnlocked(user, location);

        return List.of(
                new LessonSummaryResponse(
                        location.getId(),
                        location.getId(),
                        getLessonTitle(location),
                        location.getSlug()
                )
        );
    }

    @Transactional
    public List<HotspotResponse> getHotspotsForLocation(UUID locationId, UUID userId) {
        Location location = findLocation(locationId);
        User user = resolveUser(userId);
        progressionService.assertLocationUnlocked(user, location);

        List<Activity> activities = activityRepository
                .findAllByLocationIdAndActiveTrueOrderByDisplayOrderAsc(locationId);

        return hotspotMapper.mapHotspots(location.getContentJson(), activities);
    }

    @Transactional
    public IntroDialogueSeenResponse markIntroDialogueSeen(
            UUID locationId,
            IntroDialogueSeenRequest request
    ) {
        Location location = findLocation(locationId);
        User user = resolveUser(request == null ? null : request.userId());
        progressionService.assertLocationUnlocked(user, location);

        /*
         * source is intentionally parsed but not used for progress/unlocking logic.
         * It is accepted for future reporting/debugging and to keep the endpoint contract stable.
         */
        String source = request == null ? null : request.source();
        IntroDialogueSeenSource.fromNullableString(source);

        LearnerLocationProgress progress = progressionService.getUnlockedLocationProgress(user, location);

        OffsetDateTime now = OffsetDateTime.now();

        if (!Boolean.TRUE.equals(progress.getIntroDialogueSeen())) {
            progress.setIntroDialogueSeen(true);
            progress.setIntroDialogueSeenAt(now);
        }

        Integer currentReplayCount = progress.getIntroDialogueReplayCount();
        progress.setIntroDialogueReplayCount(
                currentReplayCount == null ? 1 : currentReplayCount + 1
        );

        progress.setUpdatedAt(now);

        LearnerLocationProgress saved = learnerLocationProgressRepository.save(progress);

        return new IntroDialogueSeenResponse(
                saved.getLocation().getId(),
                saved.getIntroDialogueSeen(),
                saved.getIntroDialogueSeenAt(),
                saved.getIntroDialogueReplayCount()
        );
    }

    private User resolveUser(UUID userId) {
        if (userId == null) {
            return demoUserService.getDemoUser();
        }

        return userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + userId));
    }

    private Location findLocation(UUID locationId) {
        return locationRepository.findById(locationId)
                .orElseThrow(() -> new ResourceNotFoundException("Location not found: " + locationId));
    }

    private LocationMenuItemResponse toMenuItem(Location location, LocationStatus status) {
        return new LocationMenuItemResponse(
                location.getId(),
                location.getCity().getId(),
                location.getName(),
                location.getSlug(),
                location.getDisplayOrder(),
                toAsset(location.getBackgroundAsset()),
                status
        );
    }

    private LocationStatus getLocationStatus(User user, Location location) {
        return learnerLocationProgressRepository.findByUserAndLocation(user, location)
                .map(progress -> progress.getStatus())
                .orElse(LocationStatus.LOCKED);
    }

    private String getLessonTitle(Location location) {
        Map<String, Object> contentJson = location.getContentJson();

        if (contentJson == null) {
            return location.getName();
        }

        String title = JsonValueReader.getString(contentJson, "title");
        return title == null ? location.getName() : title;
    }

    private AssetMetadataResponse toAsset(StaticAsset asset) {
        return AssetResponseMapper.toMetadataResponse(asset);
    }
}
