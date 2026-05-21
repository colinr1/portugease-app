package com.portugease.activity;

import com.portugease.common.enums.ActivityType;
import com.portugease.common.enums.AttemptResult;
import com.portugease.user.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface ActivityAttemptRepository extends JpaRepository<ActivityAttempt, UUID> {

    List<ActivityAttempt> findAllByUserAndActivityOrderByCreatedAtDesc(User user, Activity activity);

    long countByUserAndActivity(User user, Activity activity);

    List<ActivityAttempt> findTop10ByUserAndActivityTypeOrderByCreatedAtDesc(
            User user,
            ActivityType activityType
    );

    List<ActivityAttempt> findTop5ByUserAndActivityTypeAndResultOrderByCreatedAtDesc(
            User user,
            ActivityType activityType,
            AttemptResult result
    );
}