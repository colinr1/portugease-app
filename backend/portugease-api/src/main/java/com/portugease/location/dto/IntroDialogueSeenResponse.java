package com.portugease.location.dto;

import java.time.OffsetDateTime;
import java.util.UUID;

public record IntroDialogueSeenResponse(
        UUID locationId,
        Boolean introDialogueSeen,
        OffsetDateTime introDialogueSeenAt,
        Integer introDialogueReplayCount
) {
}