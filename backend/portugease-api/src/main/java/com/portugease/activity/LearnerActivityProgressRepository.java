package com.portugease.activity;

import com.portugease.common.enums.ActivityType;
import com.portugease.user.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface LearnerActivityProgressRepository extends JpaRepository<LearnerActivityProgress, UUID> {
    Optional<LearnerActivityProgress> findByUserAndActivity(User user, Activity activity);

    List<LearnerActivityProgress> findTop5ByUserAndActivity_ActivityTypeAndCompletedTrueOrderByCompletedAtDesc(
            User user,
            ActivityType activityType
    );
}