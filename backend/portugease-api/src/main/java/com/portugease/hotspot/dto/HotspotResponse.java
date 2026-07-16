package com.portugease.hotspot.dto;

import com.portugease.common.enums.ActivityType;

import java.util.Map;
import java.util.UUID;

public record HotspotResponse(
        String id,
        String label,
        Double xPercent,
        Double yPercent,
        Boolean visible,
        String activityKey,
        UUID activityId,
        ActivityType activityType,

        String hotspotType,
        String dialogueId,
        String ariaLabel,

        Map<String, Object> vocabulary,

        Map<String, Object> raw
) {
}
