package com.portugease.location.dto;

import com.portugease.asset.dto.AssetMetadataResponse;
import com.portugease.common.enums.LocationStatus;
import com.portugease.hotspot.dto.HotspotResponse;
import com.portugease.lesson.dto.LessonSummaryResponse;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public record LocationDetailResponse(
        UUID id,
        UUID cityId,
        String citySlug,
        String name,
        String slug,
        Integer displayOrder,
        LocationStatus status,
        AssetMetadataResponse backgroundImage,
        Map<String, Object> content,
        List<HotspotResponse> hotspots,
        List<LessonSummaryResponse> lessons
) {
}
