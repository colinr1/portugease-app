package com.portugease.lesson.dto;

import java.util.UUID;

public record LessonSummaryResponse(
        UUID id,
        UUID locationId,
        String title,
        String slug,
        String description,
        Integer estimatedMinutes
) {
}