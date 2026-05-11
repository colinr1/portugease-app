package com.portugease.progress;

import com.portugease.city.City;
import com.portugease.user.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface LearnerCityProgressRepository extends JpaRepository<LearnerCityProgress, UUID> {
    Optional<LearnerCityProgress> findByUserAndCity(User user, City city);

    List<LearnerCityProgress> findAllByUser(User user);
}