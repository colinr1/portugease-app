package com.portugease.location.dto;

import com.portugease.asset.dto.AssetMetadataResponse;
import com.portugease.common.enums.LocationStatus;

import java.util.UUID;

public record LocationMenuItemResponse(
        UUID id,
        UUID cityId,
        String name,
        String slug,
        String description,
        Integer displayOrder,
        Integer estimatedMinutes,
        AssetMetadataResponse backgroundImage,
        LocationStatus status
) {
}