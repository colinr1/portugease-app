package com.portugease.activity.dto;

import com.portugease.progress.dto.ProgressionUpdateResponse;

import java.util.UUID;

public record ActivityAttemptSubmissionResponse(
        UUID attemptId,
        UUID activityId,
        boolean isCorrect,
        int score,
        int maxScore,
        String feedbackMessage,
        String explanation,
        AdaptiveDifficultyResponse adaptiveDifficulty,
        ProgressUpdateSummaryResponse progressUpdate,
        ProgressionUpdateResponse progressionUpdate
) {
}
