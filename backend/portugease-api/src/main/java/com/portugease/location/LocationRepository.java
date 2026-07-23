package com.portugease.location;

import com.portugease.city.City;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface LocationRepository extends JpaRepository<Location, UUID> {

    Optional<Location> findBySlug(String slug);

    Optional<Location> findFirstByCityAndActiveTrueOrderByDisplayOrderAsc(City city);

    Optional<Location> findFirstByCityAndActiveTrueAndDisplayOrderGreaterThanOrderByDisplayOrderAsc(
            City city,
            Integer displayOrder
    );

    List<Location> findAllByCityAndActiveTrueOrderByDisplayOrderAsc(City city);

    List<Location> findAllByCityIdAndActiveTrueOrderByDisplayOrderAsc(UUID cityId);

    long countByCityAndActiveTrue(City city);
}
