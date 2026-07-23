package com.portugease.activity;

import com.portugease.common.enums.ActivityType;
import com.portugease.common.json.JsonValueReader;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Component
public class ActivityDefinitionSanitizer {

    public Map<String, Object> sanitize(ActivityType activityType, Map<String, Object> definition) {
        if (definition == null || definition.isEmpty()) {
            return Map.of();
        }

        return switch (activityType) {
            case SENTENCE_BUILDING -> sanitizeSentenceBuilding(definition);
            case LISTENING -> sanitizeListening(definition);
            case WORD_MATCHING -> sanitizeWordMatching(definition);
            case MULTIPLE_CHOICE, SCENARIO_CHALLENGE -> sanitizeMultipleChoice(definition);
            case SENTENCE_TRANSFORMATION -> sanitizeSentenceTransformation(definition);
        };
    }

    private Map<String, Object> sanitizeSentenceBuilding(Map<String, Object> definition) {
        Map<String, Object> sanitized = baseDefinition(definition);
        putIfPresent(sanitized, "tokens", JsonValueReader.asStringList(definition.get("tokens")));
        return sanitized;
    }

    private Map<String, Object> sanitizeListening(Map<String, Object> definition) {
        Map<String, Object> sanitized = baseDefinition(definition);
        putIfPresent(sanitized, "audioUrl", JsonValueReader.getString(definition, "audioUrl"));
        putIfPresent(sanitized, "correctAnswer", JsonValueReader.getString(definition, "correctAnswer"));
        return sanitized;
    }

    private Map<String, Object> sanitizeWordMatching(Map<String, Object> definition) {
        Map<String, Object> sanitized = baseDefinition(definition);
        List<Map<String, Object>> pairs = JsonValueReader.asMapList(definition.get("pairs"));

        List<String> leftItems = pairs.stream()
                .map(pair -> JsonValueReader.getString(pair, "left"))
                .filter(Objects::nonNull)
                .toList();

        List<String> rightItems = pairs.stream()
                .map(pair -> JsonValueReader.getString(pair, "right"))
                .filter(Objects::nonNull)
                .sorted(String.CASE_INSENSITIVE_ORDER)
                .toList();

        putIfPresent(sanitized, "leftItems", leftItems);
        putIfPresent(sanitized, "rightItems", rightItems);
        return sanitized;
    }

    private Map<String, Object> sanitizeMultipleChoice(Map<String, Object> definition) {
        Map<String, Object> sanitized = baseDefinition(definition);
        putIfPresent(sanitized, "question", JsonValueReader.getString(definition, "question"));
        putIfPresent(sanitized, "options", sanitizeOptions(definition.get("options")));
        return sanitized;
    }

    private Map<String, Object> sanitizeSentenceTransformation(Map<String, Object> definition) {
        Map<String, Object> sanitized = baseDefinition(definition);
        putIfPresent(sanitized, "prompt", JsonValueReader.getString(definition, "prompt"));
        putIfPresent(sanitized, "sourceSentence", JsonValueReader.getString(definition, "sourceSentence"));
        return sanitized;
    }

    private Map<String, Object> baseDefinition(Map<String, Object> definition) {
        Map<String, Object> sanitized = new LinkedHashMap<>();
        putIfPresent(sanitized, "hints", sanitizeHints(definition.get("hints")));
        return sanitized;
    }

    private List<Map<String, Object>> sanitizeHints(Object value) {
        List<Map<String, Object>> hints = new ArrayList<>();

        for (Map<String, Object> hint : JsonValueReader.asMapList(value)) {
            String text = JsonValueReader.getString(hint, "text");
            if (text == null) {
                continue;
            }

            Map<String, Object> sanitizedHint = new LinkedHashMap<>();
            sanitizedHint.put("text", text);
            hints.add(sanitizedHint);
        }

        return hints;
    }

    private List<Map<String, Object>> sanitizeOptions(Object value) {
        List<Map<String, Object>> options = new ArrayList<>();

        for (Map<String, Object> option : JsonValueReader.asMapList(value)) {
            Map<String, Object> sanitizedOption = new LinkedHashMap<>();
            putIfPresent(sanitizedOption, "id", JsonValueReader.getString(option, "id"));
            putIfPresent(sanitizedOption, "text", JsonValueReader.getString(option, "text"));

            if (!sanitizedOption.isEmpty()) {
                options.add(sanitizedOption);
            }
        }

        return options;
    }

    private void putIfPresent(Map<String, Object> target, String key, Object value) {
        if (value == null) {
            return;
        }

        if (value instanceof List<?> list && list.isEmpty()) {
            return;
        }

        target.put(key, value);
    }
}
