package com.portugease.common.json;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public final class JsonValueReader {

    private JsonValueReader() {
    }

    public static String getString(Map<String, Object> source, String key) {
        return asString(source.get(key));
    }

    public static String getStringOrDefault(
            Map<String, Object> source,
            String key,
            String fallback
    ) {
        String value = getString(source, key);
        return value == null || value.isBlank() ? fallback : value;
    }

    public static Double getDouble(Map<String, Object> source, String key) {
        return asDouble(source.get(key));
    }

    public static Boolean getBooleanOrDefault(
            Map<String, Object> source,
            String key,
            boolean defaultValue
    ) {
        Object value = source.get(key);
        return value == null ? defaultValue : asBoolean(value);
    }

    public static Map<String, Object> getMap(Map<String, Object> source, String key) {
        return asMap(source.get(key));
    }

    public static List<Map<String, Object>> asMapList(Object value) {
        if (!(value instanceof List<?> list)) {
            return List.of();
        }

        return list.stream()
                .map(JsonValueReader::asMap)
                .filter(Objects::nonNull)
                .toList();
    }

    public static List<String> asStringList(Object value) {
        if (!(value instanceof List<?> list)) {
            return List.of();
        }

        return list.stream()
                .filter(Objects::nonNull)
                .map(Object::toString)
                .toList();
    }

    public static String asString(Object value) {
        return value == null ? null : value.toString();
    }

    public static Boolean asBoolean(Object value) {
        if (value instanceof Boolean booleanValue) {
            return booleanValue;
        }

        if (value == null) {
            return false;
        }

        return Boolean.parseBoolean(value.toString());
    }

    public static Double asDouble(Object value) {
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

    public static Map<String, Object> asMap(Object value) {
        if (!(value instanceof Map<?, ?> rawMap)) {
            return null;
        }

        Map<String, Object> map = new LinkedHashMap<>();
        rawMap.forEach((key, mapValue) -> {
            if (key instanceof String stringKey) {
                map.put(stringKey, mapValue);
            }
        });

        return map;
    }
}
