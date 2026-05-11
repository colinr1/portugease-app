package com.portugease.progress;

import com.portugease.location.Location;
import com.portugease.user.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface LearnerLocationProgressRepository extends JpaRepository<LearnerLocationProgress, UUID> {
    Optional<LearnerLocationProgress> findByUserAndLocation(User user, Location location);

    List<LearnerLocationProgress> findAllByUser(User user);
}