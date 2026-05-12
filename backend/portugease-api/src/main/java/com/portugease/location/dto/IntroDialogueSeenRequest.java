package com.portugease.location.dto;

import java.util.UUID;

public record IntroDialogueSeenRequest(
        UUID userId,
        String source
) {
}