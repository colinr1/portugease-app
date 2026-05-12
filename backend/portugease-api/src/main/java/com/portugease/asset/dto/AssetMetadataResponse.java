package com.portugease.asset.dto;

import com.portugease.common.enums.AssetType;

import java.util.UUID;

public record AssetMetadataResponse(
        UUID id,
        String assetKey,
        AssetType assetType,
        String filePath,
        String altText,
        String description,
        String mimeType
) {
}