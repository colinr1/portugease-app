package com.portugease.progress.dto;

public record ProgressionUpdateResponse(
        boolean locationCompleted,
        UnlockedLocationResponse unlockedLocation,
        boolean cityCompleted,
        UnlockedCityResponse unlockedCity
) {
    public static ProgressionUpdateResponse none() {
        return new ProgressionUpdateResponse(false, null, false, null);
    }
}
