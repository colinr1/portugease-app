package com.portugease.asset;

import com.portugease.asset.dto.StaticAssetResponse;
import com.portugease.common.enums.AssetType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class StaticAssetService {

    private final StaticAssetRepository staticAssetRepository;

    public StaticAssetService(StaticAssetRepository staticAssetRepository) {
        this.staticAssetRepository = staticAssetRepository;
    }

    @Transactional(readOnly = true)
    public List<StaticAssetResponse> getAssets(AssetType assetType) {
        List<StaticAsset> assets = assetType == null
                ? staticAssetRepository.findAll()
                : staticAssetRepository.findByAssetType(assetType);

        return assets.stream()
                .map(this::toResponse)
                .toList();
    }

    private StaticAssetResponse toResponse(StaticAsset asset) {
        return new StaticAssetResponse(
                asset.getId(),
                asset.getAssetKey(),
                asset.getAssetType(),
                asset.getFilePath(),
                asset.getAltText(),
                asset.getDescription(),
                asset.getMimeType(),
                asset.getCitySlug(),
                asset.getLocationSlug(),
                asset.getMetadataJson()
        );
    }
}