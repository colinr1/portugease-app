package com.portugease.activity.dto;

import com.portugease.common.enums.DifficultyLevel;
import jakarta.validation.constraints.NotNull;

import java.util.Map;
import java.util.UUID;

public record ActivityAttemptSubmissionRequest(
        UUID userId,
        UUID learnerSessionId,

        @NotNull
        Map<String, Object> submittedAnswer,

        DifficultyLevel selectedDifficulty,
        Integer incorrectSubmissionCount
) {
}