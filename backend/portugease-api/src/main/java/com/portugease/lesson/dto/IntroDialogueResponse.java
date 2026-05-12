package com.portugease.lesson.dto;

import java.util.List;

public record IntroDialogueResponse(
        String id,
        String title,
        String description,
        Boolean autoOpenOnFirstVisit,
        Boolean alreadySeen,
        String hotspotId,
        List<String> targetLearningItemKeys,
        List<IntroDialogueLineResponse> lines
) {
}