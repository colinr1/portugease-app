package com.portugease.lesson.dto;

import java.util.List;

public record IntroDialogueResponse(
        String id,
        Boolean autoOpenOnFirstVisit,
        Boolean alreadySeen,
        List<IntroDialogueLineResponse> lines
) {
}
