package com.portugease.city.dto;

import com.portugease.asset.dto.AssetMetadataResponse;
import com.portugease.common.enums.CityStatus;

import java.util.UUID;

public record CityListItemResponse(
        UUID id,
        String name,
        String slug,
        String description,
        Integer displayOrder,
        CityMarkerResponse marker,
        AssetMetadataResponse backgroundImage,
        CityStatus status
) {
}