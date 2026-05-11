package com.portugease.asset;

import com.portugease.common.enums.AssetType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface StaticAssetRepository extends JpaRepository<StaticAsset, UUID> {
    List<StaticAsset> findByAssetType(AssetType assetType);
}