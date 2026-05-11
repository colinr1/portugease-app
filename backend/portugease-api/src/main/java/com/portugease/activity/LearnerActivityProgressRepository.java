package com.portugease.activity;

import com.portugease.user.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface LearnerActivityProgressRepository extends JpaRepository<LearnerActivityProgress, UUID> {
    Optional<LearnerActivityProgress> findByUserAndActivity(User user, Activity activity);
}