package com.portugease.hotspot;

import com.portugease.activity.Activity;
import com.portugease.hotspot.dto.HotspotResponse;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Objects;
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

    @SuppressWarnings("unchecked")
    private List<Map<String, Object>> extractHotspotMaps(Map<String, Object> contentJson) {
        Object hotspots = contentJson.get("hotspots");

        if (!(hotspots instanceof List<?> hotspotList)) {
            return List.of();
        }

        return hotspotList.stream()
                .filter(Map.class::isInstance)
                .map(item -> (Map<String, Object>) item)
                .toList();
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> getMap(Map<String, Object> source, String key) {
        Object value = source.get(key);

        if (value instanceof Map<?, ?> mapValue) {
            return (Map<String, Object>) mapValue;
        }

        return null;
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
        Object value = map.get(key);
        return value == null ? null : value.toString();
    }

    private Double getDouble(Map<String, Object> map, String key) {
        Object value = map.get(key);

        if (value instanceof Number number) {
            return number.doubleValue();
        }

        if (value instanceof String stringValue && !stringValue.isBlank()) {
            try {
                return Double.parseDouble(stringValue);
            } catch (NumberFormatException exception) {
                return null;
            }
        }

        return null;
    }

    private Boolean getBooleanOrDefault(Map<String, Object> map, String key, boolean defaultValue) {
        Object value = map.get(key);

        if (value instanceof Boolean booleanValue) {
            return booleanValue;
        }

        if (Objects.isNull(value)) {
            return defaultValue;
        }

        return Boolean.parseBoolean(value.toString());
    }
}