package com.portugease.city.dto;

import com.portugease.asset.dto.AssetMetadataResponse;
import com.portugease.common.enums.CityStatus;
import com.portugease.location.dto.LocationMenuItemResponse;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public record CityDetailResponse(
        UUID id,
        String name,
        String slug,
        String description,
        Integer displayOrder,
        CityMarkerResponse marker,
        AssetMetadataResponse backgroundImage,
        CityStatus status,
        Map<String, Object> unlockRule,
        List<LocationMenuItemResponse> locations
) {
}