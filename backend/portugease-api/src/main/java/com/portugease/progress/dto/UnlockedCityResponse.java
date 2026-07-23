package com.portugease.progress.dto;

import java.util.UUID;

public record UnlockedCityResponse(
        UUID cityId,
        String citySlug,
        String cityName,
        UnlockedLocationResponse firstUnlockedLocation
) {
}
