package com.portugease.activity.dto;

import com.portugease.common.enums.ActivityType;

import java.util.Map;
import java.util.UUID;

public record ActivityResponse(
        UUID id,
        UUID locationId,
        String hotspotId,
        String activityKey,
        ActivityType activityType,
        String title,
        String instructions,
        Map<String, Object> definitionJson,
        Map<String, Object> learningItemsJson,
        Integer maxScore,
        Boolean requiredForCompletion
) {
}