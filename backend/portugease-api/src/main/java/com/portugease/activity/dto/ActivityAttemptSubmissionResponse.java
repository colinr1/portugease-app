package com.portugease.activity.dto;

import java.util.UUID;

public record ActivityAttemptSubmissionResponse(
        UUID attemptId,
        UUID activityId,
        boolean isCorrect,
        int score,
        int maxScore,
        String feedbackMessage,
        String explanation,
        AdaptiveSupportDecisionResponse adaptiveSupport,
        ProgressUpdateSummaryResponse progressUpdate
) {
}