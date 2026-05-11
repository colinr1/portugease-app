package com.portugease.statistics;

import com.portugease.user.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface LearnerItemStatisticsRepository extends JpaRepository<LearnerItemStatistics, UUID> {
    Optional<LearnerItemStatistics> findByUserAndItemKey(User user, String itemKey);

    List<LearnerItemStatistics> findAllByUserAndNeedsReviewTrue(User user);
}