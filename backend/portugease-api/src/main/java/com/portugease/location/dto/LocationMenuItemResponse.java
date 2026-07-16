package com.portugease.location.dto;

import com.portugease.asset.dto.AssetMetadataResponse;
import com.portugease.common.enums.LocationStatus;

import java.util.UUID;

public record LocationMenuItemResponse(
        UUID id,
        UUID cityId,
        String name,
        String slug,
        Integer displayOrder,
        AssetMetadataResponse backgroundImage,
        LocationStatus status
) {
}
