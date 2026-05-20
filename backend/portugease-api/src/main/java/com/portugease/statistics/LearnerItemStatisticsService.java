package com.portugease.statistics;

import com.portugease.statistics.dto.LearnerItemStatisticsResponse;
import com.portugease.user.DemoUserService;
import com.portugease.user.User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class LearnerItemStatisticsService {

    private final LearnerItemStatisticsRepository repository;
    private final DemoUserService demoUserService;

    public LearnerItemStatisticsService(
            LearnerItemStatisticsRepository repository,
            DemoUserService demoUserService
    ) {
        this.repository = repository;
        this.demoUserService = demoUserService;
    }

    @Transactional(readOnly = true)
    public List<LearnerItemStatisticsResponse> getItemsNeedingReview() {
        User user = demoUserService.getDemoUser();

        return repository.findAllByUserAndNeedsReviewTrue(user)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    private LearnerItemStatisticsResponse toResponse(LearnerItemStatistics item) {
        return new LearnerItemStatisticsResponse(
                item.getId(),
                item.getItemKey(),
                item.getItemType(),
                item.getItemText(),
                item.getTimesSeen(),
                item.getCorrectCount(),
                item.getIncorrectCount(),
                item.getDifficultyScore(),
                item.getNeedsReview(),
                item.getLastSeenAt()
        );
    }
}