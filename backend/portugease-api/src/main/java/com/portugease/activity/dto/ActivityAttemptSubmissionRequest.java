package com.portugease.activity.dto;

import jakarta.validation.constraints.NotNull;

import java.util.Map;
import java.util.UUID;

public record ActivityAttemptSubmissionRequest(
        UUID userId,
        UUID learnerSessionId,

        @NotNull
        Map<String, Object> submittedAnswer,

        Integer hintsUsed
) {
}