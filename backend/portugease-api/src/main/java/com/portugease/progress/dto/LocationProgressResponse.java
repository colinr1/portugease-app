package com.portugease.progress.dto;

import com.portugease.common.enums.LocationStatus;

import java.time.OffsetDateTime;
import java.util.UUID;

public record LocationProgressResponse(
        UUID locationId,
        String locationSlug,
        String locationName,
        LocationStatus status,
        Integer score,
        Integer completedActivitiesCount,
        Integer totalRequiredActivitiesCount,
        OffsetDateTime completedAt
) {
}