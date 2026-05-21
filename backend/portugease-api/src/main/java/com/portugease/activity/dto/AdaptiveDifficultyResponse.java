package com.portugease.activity.dto;

public record AdaptiveDifficultyResponse(
        String selectedDifficulty,
        boolean difficultyChanged,
        String newDifficulty,
        String difficultyChangeMessage
) {
}