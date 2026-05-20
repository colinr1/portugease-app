package com.portugease.statistics.dto;

import com.portugease.common.enums.LearningItemType;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

public record LearnerItemStatisticsResponse(
        UUID id,
        String itemKey,
        LearningItemType itemType,
        String itemText,
        Integer timesSeen,
        Integer correctCount,
        Integer incorrectCount,
        BigDecimal difficultyScore,
        Boolean needsReview,
        OffsetDateTime lastSeenAt
) {
}