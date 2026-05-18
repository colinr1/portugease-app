package com.portugease.progress.dto;

import com.portugease.common.enums.CityStatus;

import java.time.OffsetDateTime;
import java.util.UUID;

public record CityProgressResponse(
        UUID cityId,
        String citySlug,
        String cityName,
        CityStatus status,
        Integer score,
        Integer completedLocationsCount,
        Integer totalLocationsCount,
        OffsetDateTime completedAt
) {
}