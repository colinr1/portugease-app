package com.portugease.lesson.dto;

import java.util.List;

public record IntroDialogueLineResponse(
        String speaker,
        String portugueseText,
        String englishTranslation,
        String audioPath,
        List<IntroDialogueFocusMarkerResponse> focusMarkers
) {
}
