package com.portugease.hotspot;

import com.portugease.activity.Activity;
import com.portugease.common.json.JsonValueReader;
import com.portugease.hotspot.dto.HotspotResponse;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
public class HotspotMapper {

    public List<HotspotResponse> mapHotspots(
            Map<String, Object> contentJson,
            List<Activity> activities
    ) {
        if (contentJson == null || contentJson.isEmpty()) {
            return List.of();
        }

        List<Map<String, Object>> hotspotMaps = extractHotspotMaps(contentJson);

        Map<String, Activity> activitiesByKey = activities.stream()
                .collect(Collectors.toMap(
                        Activity::getActivityKey,
                        Function.identity(),
                        (existing, replacement) -> existing
                ));

        return hotspotMaps.stream()
                .map(rawHotspot -> toResponse(rawHotspot, activitiesByKey))
                .toList();
    }

    private List<Map<String, Object>> extractHotspotMaps(Map<String, Object> contentJson) {
        return JsonValueReader.asMapList(contentJson.get("hotspots"));
    }

    private Map<String, Object> getMap(Map<String, Object> source, String key) {
        return JsonValueReader.getMap(source, key);
    }

    private HotspotResponse toResponse(
            Map<String, Object> rawHotspot,
            Map<String, Activity> activitiesByKey
    ) {
        String activityKey = getString(rawHotspot, "activityKey");
        Activity activity = activityKey == null ? null : activitiesByKey.get(activityKey);

        return new HotspotResponse(
                getString(rawHotspot, "id"),
                getString(rawHotspot, "label"),
                getDouble(rawHotspot, "xPercent"),
                getDouble(rawHotspot, "yPercent"),
                getString(rawHotspot, "iconAssetKey"),
                getBooleanOrDefault(rawHotspot, "visible", true),
                activityKey,
                activity != null ? activity.getId() : null,
                activity != null ? activity.getActivityType() : null,

                getString(rawHotspot, "hotspotType"),
                getString(rawHotspot, "style"),
                getString(rawHotspot, "dialogueId"),
                getString(rawHotspot, "ariaLabel"),

                getMap(rawHotspot, "vocabulary"),

                rawHotspot
        );
    }

    private String getString(Map<String, Object> map, String key) {
        return JsonValueReader.getString(map, key);
    }

    private Double getDouble(Map<String, Object> map, String key) {
        return JsonValueReader.getDouble(map, key);
    }

    private Boolean getBooleanOrDefault(Map<String, Object> map, String key, boolean defaultValue) {
        return JsonValueReader.getBooleanOrDefault(map, key, defaultValue);
    }
}
