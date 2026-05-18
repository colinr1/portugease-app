package com.portugease.lesson.dto;

public record IntroDialogueFocusMarkerResponse(
        String id,
        Double xPercent,
        Double yPercent,
        String ariaLabel
) {
}