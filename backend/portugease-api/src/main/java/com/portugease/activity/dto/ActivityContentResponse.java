package com.portugease.activity.dto;

import com.portugease.common.enums.ActivityType;

import java.util.Map;
import java.util.UUID;

public record ActivityContentResponse(
        UUID id,
        UUID locationId,
        String hotspotId,
        String activityKey,
        ActivityType activityType,
        String title,
        String instructions,
        Map<String, Object> definition,
        Map<String, Object> learningItems,
        Integer maxScore,
        Boolean requiredForCompletion,
        Integer displayOrder
) {
}