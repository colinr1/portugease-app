package com.portugease.activity.dto;

public record ProgressUpdateSummaryResponse(
        boolean activityCompleted,
        boolean activityMastered,
        int attemptsCount,
        int incorrectAttemptsCount,
        int bestScore,
        int maxScore,
        boolean completedPerfectly,
        int incorrectBeforeSuccess
) {
}