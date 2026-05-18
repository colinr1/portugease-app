package com.portugease.progress;

import com.portugease.progress.dto.CityProgressResponse;
import com.portugease.progress.dto.LocationProgressResponse;
import com.portugease.progress.dto.ProgressResponse;
import com.portugease.user.DemoUserService;
import com.portugease.user.User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ProgressService {

    private final DemoUserService demoUserService;
    private final LearnerCityProgressRepository cityProgressRepository;
    private final LearnerLocationProgressRepository locationProgressRepository;

    public ProgressService(
            DemoUserService demoUserService,
            LearnerCityProgressRepository cityProgressRepository,
            LearnerLocationProgressRepository locationProgressRepository
    ) {
        this.demoUserService = demoUserService;
        this.cityProgressRepository = cityProgressRepository;
        this.locationProgressRepository = locationProgressRepository;
    }

    @Transactional(readOnly = true)
    public ProgressResponse getDemoProgress() {
        User user = demoUserService.getDemoUser();

        List<CityProgressResponse> cityProgress = cityProgressRepository.findAllByUser(user)
                .stream()
                .map(progress -> new CityProgressResponse(
                        progress.getCity().getId(),
                        progress.getCity().getSlug(),
                        progress.getCity().getName(),
                        progress.getStatus(),
                        progress.getScore(),
                        progress.getCompletedLocationsCount(),
                        progress.getTotalLocationsCount(),
                        progress.getCompletedAt()
                ))
                .toList();

        List<LocationProgressResponse> locationProgress = locationProgressRepository.findAllByUser(user)
                .stream()
                .map(progress -> new LocationProgressResponse(
                        progress.getLocation().getId(),
                        progress.getLocation().getSlug(),
                        progress.getLocation().getName(),
                        progress.getStatus(),
                        progress.getScore(),
                        progress.getCompletedActivitiesCount(),
                        progress.getTotalRequiredActivitiesCount(),
                        progress.getCompletedAt()
                ))
                .toList();

        return new ProgressResponse(cityProgress, locationProgress);
    }
}