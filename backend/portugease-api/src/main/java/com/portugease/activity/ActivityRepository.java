package com.portugease.activity;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ActivityRepository extends JpaRepository<Activity, UUID> {

    Optional<Activity> findByActivityKey(String activityKey);

    List<Activity> findAllByLocationIdAndActiveTrueOrderByDisplayOrderAsc(UUID locationId);

    List<Activity> findAllByLocation_SlugAndActiveTrueOrderByDisplayOrderAsc(String locationSlug);

}