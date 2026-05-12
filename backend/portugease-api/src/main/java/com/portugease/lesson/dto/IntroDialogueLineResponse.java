package com.portugease.lesson.dto;

import java.util.List;

public record IntroDialogueLineResponse(
        String id,
        String speaker,
        String portugueseText,
        String englishTranslation,
        String audioPath,
        List<String> targetLearningItemKeys
) {
}