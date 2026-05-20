package com.portugease.asset;

import com.portugease.asset.dto.StaticAssetResponse;
import com.portugease.common.enums.AssetType;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/assets")
public class StaticAssetController {

    private final StaticAssetService staticAssetService;

    public StaticAssetController(StaticAssetService staticAssetService) {
        this.staticAssetService = staticAssetService;
    }

    @GetMapping
    public List<StaticAssetResponse> getAssets(
            @RequestParam(required = false) AssetType assetType
    ) {
        return staticAssetService.getAssets(assetType);
    }
}