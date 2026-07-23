package com.portugease.city;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CityRepository extends JpaRepository<City, UUID> {

    Optional<City> findBySlug(String slug);

    Optional<City> findFirstByActiveTrueOrderByDisplayOrderAsc();

    Optional<City> findFirstByActiveTrueAndDisplayOrderGreaterThanOrderByDisplayOrderAsc(Integer displayOrder);

    List<City> findAllByActiveTrueOrderByDisplayOrderAsc();
}
