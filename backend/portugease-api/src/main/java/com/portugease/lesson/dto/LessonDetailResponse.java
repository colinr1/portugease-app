package com.portugease.lesson.dto;

import com.portugease.activity.dto.ActivityContentResponse;
import com.portugease.asset.dto.AssetMetadataResponse;
import com.portugease.hotspot.dto.HotspotResponse;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public record LessonDetailResponse(
        UUID id,
        UUID locationId,
        UUID cityId,
        String title,
        String slug,
        String description,
        Integer estimatedMinutes,
        AssetMetadataResponse backgroundImage,
        Map<String, Object> content,
        List<HotspotResponse> hotspots,
        List<ActivityContentResponse> activities,
        IntroDialogueResponse introDialogue
) {
}