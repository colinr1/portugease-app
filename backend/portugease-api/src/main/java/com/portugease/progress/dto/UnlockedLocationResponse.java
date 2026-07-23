package com.portugease.progress.dto;

import java.util.UUID;

public record UnlockedLocationResponse(
        UUID locationId,
        UUID cityId,
        String locationSlug,
        String locationName
) {
}
