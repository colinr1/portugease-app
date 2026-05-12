package com.portugease.location;

public enum IntroDialogueSeenSource {
    AUTO_OPEN_CLOSE,
    AUTO_OPEN_FINISH,
    HOTSPOT_CLOSE,
    HOTSPOT_FINISH;

    public static IntroDialogueSeenSource fromNullableString(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }

        try {
            return IntroDialogueSeenSource.valueOf(value.trim().toUpperCase());
        } catch (IllegalArgumentException exception) {
            return null;
        }
    }
}