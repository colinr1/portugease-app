package com.portugease.city.dto;

import java.util.Map;

public record CityMarkerResponse(
        Double xPercent,
        Double yPercent,
        String iconAssetKey,
        Map<String, Object> raw
) {
}