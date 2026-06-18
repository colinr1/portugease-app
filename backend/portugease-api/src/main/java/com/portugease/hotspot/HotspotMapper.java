package com.portugease.hotspot;

import com.portugease.activity.Activity;
import com.portugease.common.json.JsonValueReader;
import com.portugease.hotspot.dto.HotspotResponse;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
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
        Map<String, Object> vocabulary = mapVocabulary(rawHotspot);

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

                vocabulary,

                mapRawHotspot(rawHotspot, vocabulary)
        );
    }

    private Map<String, Object> mapVocabulary(Map<String, Object> rawHotspot) {
        Map<String, Object> vocabulary = getMap(rawHotspot, "vocabulary");

        if (vocabulary == null) {
            return null;
        }

        Map<String, Object> mappedVocabulary = new LinkedHashMap<>(vocabulary);
        normalizeAudioReference(mappedVocabulary, "audioPath");
        copyAudioReferenceIfMissing(rawHotspot, mappedVocabulary, "audioPath");

        return mappedVocabulary;
    }

    private Map<String, Object> mapRawHotspot(
            Map<String, Object> rawHotspot,
            Map<String, Object> vocabulary
    ) {
        Map<String, Object> mappedRawHotspot = new LinkedHashMap<>(rawHotspot);
        normalizeAudioReference(mappedRawHotspot, "audioPath");

        if (vocabulary != null) {
            mappedRawHotspot.put("vocabulary", vocabulary);
        }

        return mappedRawHotspot;
    }

    private void normalizeAudioReference(Map<String, Object> source, String key) {
        if (!source.containsKey(key)) {
            return;
        }

        String audioReference = frontendAudioReference(source.get(key));
        if (audioReference == null) {
            source.remove(key);
            return;
        }

        source.put(key, audioReference);
    }

    private void copyAudioReferenceIfMissing(
            Map<String, Object> rawHotspot,
            Map<String, Object> vocabulary,
            String key
    ) {
        if (vocabulary.containsKey(key)) {
            return;
        }

        String audioReference = frontendAudioReference(rawHotspot.get(key));
        if (audioReference != null) {
            vocabulary.put(key, audioReference);
        }
    }

    private String frontendAudioReference(Object value) {
        String audioReference = JsonValueReader.asString(value);

        if (audioReference == null || audioReference.isBlank()) {
            return null;
        }

        String trimmedReference = audioReference.trim();

        if (trimmedReference.startsWith("/assets/")
                || trimmedReference.startsWith("assets/")
                || trimmedReference.startsWith("https://")
                || trimmedReference.startsWith("http://")) {
            return trimmedReference;
        }

        return null;
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
