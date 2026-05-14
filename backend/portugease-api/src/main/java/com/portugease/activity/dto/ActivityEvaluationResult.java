package com.portugease.activity.dto;

import java.util.Map;

public record ActivityEvaluationResult(
        boolean correct,
        int score,
        int maxScore,
        String feedbackMessage,
        String explanation,
        Map<String, Object> evaluationJson
) {
}