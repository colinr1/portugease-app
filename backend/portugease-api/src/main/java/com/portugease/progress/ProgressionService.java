package com.portugease.progress;

import com.portugease.activity.Activity;
import com.portugease.activity.ActivityRepository;
import com.portugease.activity.LearnerActivityProgressRepository;
import com.portugease.city.City;
import com.portugease.city.CityRepository;
import com.portugease.common.enums.CityStatus;
import com.portugease.common.enums.LocationStatus;
import com.portugease.common.exception.LockedContentException;
import com.portugease.location.Location;
import com.portugease.location.LocationRepository;
import com.portugease.progress.dto.ProgressionUpdateResponse;
import com.portugease.progress.dto.UnlockedCityResponse;
import com.portugease.progress.dto.UnlockedLocationResponse;
import com.portugease.user.User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.Optional;

@Service
public class ProgressionService {

    private final CityRepository cityRepository;
    private final LocationRepository locationRepository;
    private final ActivityRepository activityRepository;
    private final LearnerActivityProgressRepository activityProgressRepository;
    private final LearnerCityProgressRepository cityProgressRepository;
    private final LearnerLocationProgressRepository locationProgressRepository;

    public ProgressionService(
            CityRepository cityRepository,
            LocationRepository locationRepository,
            ActivityRepository activityRepository,
            LearnerActivityProgressRepository activityProgressRepository,
            LearnerCityProgressRepository cityProgressRepository,
            LearnerLocationProgressRepository locationProgressRepository
    ) {
        this.cityRepository = cityRepository;
        this.locationRepository = locationRepository;
        this.activityRepository = activityRepository;
        this.activityProgressRepository = activityProgressRepository;
        this.cityProgressRepository = cityProgressRepository;
        this.locationProgressRepository = locationProgressRepository;
    }

    @Transactional
    public void ensureInitialProgress(User user) {
        cityRepository.findFirstByActiveTrueOrderByDisplayOrderAsc()
                .ifPresent(firstCity -> {
                    OffsetDateTime now = OffsetDateTime.now();
                    LearnerCityProgress cityProgress = getOrCreateCityProgress(user, firstCity);

                    if (isLocked(cityProgress.getStatus())) {
                        unlockCityProgress(cityProgress, now);
                    }

                    refreshCityCounts(cityProgress, firstCity);
                    cityProgressRepository.save(cityProgress);

                    if (!isLocked(cityProgress.getStatus())) {
                        locationRepository.findFirstByCityAndActiveTrueOrderByDisplayOrderAsc(firstCity)
                                .ifPresent(firstLocation -> unlockLocation(user, firstLocation, now));
                    }
                });
    }

    @Transactional
    public void assertCityUnlocked(User user, City city) {
        ensureInitialProgress(user);

        boolean unlocked = cityProgressRepository.findByUserAndCity(user, city)
                .map(progress -> !isLocked(progress.getStatus()))
                .orElse(false);

        if (!unlocked) {
            throw new LockedContentException("City is locked: " + city.getSlug());
        }
    }

    @Transactional
    public void assertLocationUnlocked(User user, Location location) {
        ensureInitialProgress(user);

        boolean cityUnlocked = cityProgressRepository.findByUserAndCity(user, location.getCity())
                .map(progress -> !isLocked(progress.getStatus()))
                .orElse(false);

        if (!cityUnlocked) {
            throw new LockedContentException("City is locked: " + location.getCity().getSlug());
        }

        boolean locationUnlocked = locationProgressRepository.findByUserAndLocation(user, location)
                .map(progress -> !isLocked(progress.getStatus()))
                .orElse(false);

        if (!locationUnlocked) {
            throw new LockedContentException("Location is locked: " + location.getSlug());
        }
    }

    @Transactional
    public LearnerLocationProgress getUnlockedLocationProgress(User user, Location location) {
        assertLocationUnlocked(user, location);

        return locationProgressRepository.findByUserAndLocation(user, location)
                .orElseThrow(() -> new LockedContentException("Location is locked: " + location.getSlug()));
    }

    @Transactional
    public ProgressionUpdateResponse applyProgressionAfterActivityAttempt(User user, Activity activity) {
        assertLocationUnlocked(user, activity.getLocation());

        OffsetDateTime now = OffsetDateTime.now();
        Location location = activity.getLocation();
        City city = location.getCity();

        markStarted(user, location, city, now);

        LocationProgressionResult locationResult = refreshLocationProgress(user, location, now);
        CityProgressionResult cityResult = refreshCityProgress(user, city, now);

        return new ProgressionUpdateResponse(
                locationResult.completedNow(),
                locationResult.unlockedLocation(),
                cityResult.completedNow(),
                cityResult.unlockedCity()
        );
    }

    private void markStarted(User user, Location location, City city, OffsetDateTime now) {
        LearnerLocationProgress locationProgress = getOrCreateLocationProgress(user, location);
        if (locationProgress.getStatus() == LocationStatus.UNLOCKED) {
            locationProgress.setStatus(LocationStatus.IN_PROGRESS);
        }
        if (locationProgress.getStartedAt() == null) {
            locationProgress.setStartedAt(now);
        }
        refreshLocationCounts(locationProgress, location);
        locationProgress.setUpdatedAt(now);
        locationProgressRepository.save(locationProgress);

        LearnerCityProgress cityProgress = getOrCreateCityProgress(user, city);
        if (cityProgress.getStatus() == CityStatus.UNLOCKED) {
            cityProgress.setStatus(CityStatus.IN_PROGRESS);
        }
        if (cityProgress.getStartedAt() == null) {
            cityProgress.setStartedAt(now);
        }
        refreshCityCounts(cityProgress, city);
        cityProgress.setUpdatedAt(now);
        cityProgressRepository.save(cityProgress);
    }

    private LocationProgressionResult refreshLocationProgress(
            User user,
            Location location,
            OffsetDateTime now
    ) {
        LearnerLocationProgress progress = getOrCreateLocationProgress(user, location);
        refreshLocationCounts(progress, location);

        boolean completedNow = false;
        UnlockedLocationResponse unlockedLocation = null;

        if (
                progress.getTotalRequiredActivitiesCount() > 0
                        && progress.getCompletedActivitiesCount() >= progress.getTotalRequiredActivitiesCount()
        ) {
            completedNow = progress.getStatus() != LocationStatus.COMPLETED;
            progress.setStatus(LocationStatus.COMPLETED);

            if (progress.getCompletedAt() == null) {
                progress.setCompletedAt(now);
            }

            unlockedLocation = locationRepository
                    .findFirstByCityAndActiveTrueAndDisplayOrderGreaterThanOrderByDisplayOrderAsc(
                            location.getCity(),
                            location.getDisplayOrder()
                    )
                    .flatMap(nextLocation -> unlockLocation(user, nextLocation, now))
                    .orElse(null);
        }

        progress.setUpdatedAt(now);
        locationProgressRepository.save(progress);

        return new LocationProgressionResult(completedNow, unlockedLocation);
    }

    private CityProgressionResult refreshCityProgress(User user, City city, OffsetDateTime now) {
        LearnerCityProgress progress = getOrCreateCityProgress(user, city);
        refreshCityCounts(progress, city);

        boolean completedNow = false;
        UnlockedCityResponse unlockedCity = null;

        if (
                progress.getTotalLocationsCount() > 0
                        && progress.getCompletedLocationsCount() >= progress.getTotalLocationsCount()
        ) {
            completedNow = progress.getStatus() != CityStatus.COMPLETED;
            progress.setStatus(CityStatus.COMPLETED);

            if (progress.getCompletedAt() == null) {
                progress.setCompletedAt(now);
            }

            unlockedCity = cityRepository
                    .findFirstByActiveTrueAndDisplayOrderGreaterThanOrderByDisplayOrderAsc(city.getDisplayOrder())
                    .flatMap(nextCity -> unlockCity(user, nextCity, now))
                    .orElse(null);
        }

        progress.setUpdatedAt(now);
        cityProgressRepository.save(progress);

        return new CityProgressionResult(completedNow, unlockedCity);
    }

    private Optional<UnlockedCityResponse> unlockCity(User user, City city, OffsetDateTime now) {
        LearnerCityProgress progress = getOrCreateCityProgress(user, city);
        boolean newlyUnlocked = isLocked(progress.getStatus());

        if (newlyUnlocked) {
            unlockCityProgress(progress, now);
        }

        refreshCityCounts(progress, city);
        progress.setUpdatedAt(now);
        cityProgressRepository.save(progress);

        UnlockedLocationResponse firstUnlockedLocation = locationRepository
                .findFirstByCityAndActiveTrueOrderByDisplayOrderAsc(city)
                .flatMap(firstLocation -> unlockLocation(user, firstLocation, now))
                .orElse(null);

        if (!newlyUnlocked) {
            return Optional.empty();
        }

        return Optional.of(new UnlockedCityResponse(
                city.getId(),
                city.getSlug(),
                city.getName(),
                firstUnlockedLocation
        ));
    }

    private Optional<UnlockedLocationResponse> unlockLocation(
            User user,
            Location location,
            OffsetDateTime now
    ) {
        LearnerLocationProgress progress = getOrCreateLocationProgress(user, location);
        boolean newlyUnlocked = isLocked(progress.getStatus());

        if (newlyUnlocked) {
            progress.setStatus(LocationStatus.UNLOCKED);
            if (progress.getUnlockedAt() == null) {
                progress.setUnlockedAt(now);
            }
        }

        refreshLocationCounts(progress, location);
        progress.setUpdatedAt(now);
        locationProgressRepository.save(progress);

        if (!newlyUnlocked) {
            return Optional.empty();
        }

        return Optional.of(new UnlockedLocationResponse(
                location.getId(),
                location.getCity().getId(),
                location.getSlug(),
                location.getName()
        ));
    }

    private LearnerCityProgress getOrCreateCityProgress(User user, City city) {
        return cityProgressRepository.findByUserAndCity(user, city)
                .orElseGet(() -> {
                    LearnerCityProgress progress = new LearnerCityProgress();
                    progress.setUser(user);
                    progress.setCity(city);
                    progress.setStatus(CityStatus.LOCKED);
                    return progress;
                });
    }

    private LearnerLocationProgress getOrCreateLocationProgress(User user, Location location) {
        return locationProgressRepository.findByUserAndLocation(user, location)
                .orElseGet(() -> {
                    LearnerLocationProgress progress = new LearnerLocationProgress();
                    progress.setUser(user);
                    progress.setLocation(location);
                    progress.setStatus(LocationStatus.LOCKED);
                    return progress;
                });
    }

    private void unlockCityProgress(LearnerCityProgress progress, OffsetDateTime now) {
        progress.setStatus(CityStatus.UNLOCKED);
        if (progress.getUnlockedAt() == null) {
            progress.setUnlockedAt(now);
        }
        progress.setUpdatedAt(now);
    }

    private void refreshLocationCounts(LearnerLocationProgress progress, Location location) {
        long totalRequiredActivities =
                activityRepository.countByLocationAndActiveTrueAndRequiredForCompletionTrue(location);
        long completedRequiredActivities =
                activityProgressRepository
                        .countByUserAndLocationAndCompletedTrueAndActivity_ActiveTrueAndActivity_RequiredForCompletionTrue(
                                progress.getUser(),
                                location
                        );

        progress.setTotalRequiredActivitiesCount(Math.toIntExact(totalRequiredActivities));
        progress.setCompletedActivitiesCount(Math.toIntExact(completedRequiredActivities));
    }

    private void refreshCityCounts(LearnerCityProgress progress, City city) {
        long totalLocations = locationRepository.countByCityAndActiveTrue(city);
        long completedLocations = locationProgressRepository
                .countByUserAndLocation_CityAndStatusAndLocation_ActiveTrue(
                        progress.getUser(),
                        city,
                        LocationStatus.COMPLETED
                );

        progress.setTotalLocationsCount(Math.toIntExact(totalLocations));
        progress.setCompletedLocationsCount(Math.toIntExact(completedLocations));
    }

    private boolean isLocked(CityStatus status) {
        return status == null || status == CityStatus.LOCKED;
    }

    private boolean isLocked(LocationStatus status) {
        return status == null || status == LocationStatus.LOCKED;
    }

    private record LocationProgressionResult(
            boolean completedNow,
            UnlockedLocationResponse unlockedLocation
    ) {
    }

    private record CityProgressionResult(
            boolean completedNow,
            UnlockedCityResponse unlockedCity
    ) {
    }
}
