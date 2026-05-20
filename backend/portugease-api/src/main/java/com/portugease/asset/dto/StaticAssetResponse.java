package com.portugease.asset.dto;

import com.portugease.common.enums.AssetType;

import java.util.Map;
import java.util.UUID;

public record StaticAssetResponse(
        UUID id,
        String assetKey,
        AssetType assetType,
        String filePath,
        String altText,
        String description,
        String mimeType,
        String citySlug,
        String locationSlug,
        Map<String, Object> metadataJson
) {
}