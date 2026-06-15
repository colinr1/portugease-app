package com.portugease.asset;

import com.portugease.asset.dto.AssetMetadataResponse;

public final class AssetResponseMapper {

    private AssetResponseMapper() {
    }

    public static AssetMetadataResponse toMetadataResponse(StaticAsset asset) {
        if (asset == null) {
            return null;
        }

        return new AssetMetadataResponse(
                asset.getId(),
                asset.getAssetKey(),
                asset.getAssetType(),
                asset.getFilePath(),
                asset.getAltText(),
                asset.getDescription(),
                asset.getMimeType()
        );
    }
}
