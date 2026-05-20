package com.portugease.adaptive.dto;

import com.portugease.common.enums.AdaptiveEventType;

import java.time.OffsetDateTime;
import java.util.Map;
import java.util.UUID;

public record AdaptiveEventResponse(
        UUID id,
        AdaptiveEventType eventType,
        String message,
        Map<String, Object> contextJson,
        OffsetDateTime createdAt
) {
}