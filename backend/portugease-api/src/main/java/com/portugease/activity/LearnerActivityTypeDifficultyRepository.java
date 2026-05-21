package com.portugease.activity;

import com.portugease.common.enums.ActivityType;
import com.portugease.user.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface LearnerActivityTypeDifficultyRepository
        extends JpaRepository<LearnerActivityTypeDifficulty, UUID> {

    Optional<LearnerActivityTypeDifficulty> findByUserAndActivityType(
            User user,
            ActivityType activityType
    );
}