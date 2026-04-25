package com.kecheng.market.common.log;

import jakarta.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

public final class RequestLogFormatter {

    private static final int MAX_VALUE_LENGTH = 80;
    private static final int MAX_QUERY_LENGTH = 256;

    private RequestLogFormatter() {
    }

    public static String clientIp(HttpServletRequest request) {
        String forwardedFor = request.getHeader("X-Forwarded-For");
        if (forwardedFor != null && !forwardedFor.isBlank()) {
            return forwardedFor.split(",")[0].trim();
        }
        String realIp = request.getHeader("X-Real-IP");
        if (realIp != null && !realIp.isBlank()) {
            return realIp.trim();
        }
        return request.getRemoteAddr();
    }

    public static String formatQuery(HttpServletRequest request) {
        Map<String, String[]> parameterMap = request.getParameterMap();
        if (parameterMap == null || parameterMap.isEmpty()) {
            return "-";
        }
        Map<String, String[]> sorted = new TreeMap<>(parameterMap);
        List<String> entries = new ArrayList<>();
        for (Map.Entry<String, String[]> entry : sorted.entrySet()) {
            String key = entry.getKey();
            String value = formatValues(key, entry.getValue());
            entries.add(key + "=" + value);
        }
        return abbreviate(String.join("&", entries), MAX_QUERY_LENGTH);
    }

    public static String normalize(String value) {
        if (value == null || value.isBlank()) {
            return "-";
        }
        return abbreviate(value.replaceAll("[\\r\\n\\t]+", " ").trim(), MAX_QUERY_LENGTH);
    }

    private static String formatValues(String key, String[] values) {
        if (isSensitiveKey(key)) {
            return "***";
        }
        if (values == null || values.length == 0) {
            return "-";
        }
        return Arrays.stream(values)
                .map(value -> abbreviate(value, MAX_VALUE_LENGTH))
                .collect(Collectors.joining(","));
    }

    private static boolean isSensitiveKey(String key) {
        String normalized = key == null ? "" : key.toLowerCase();
        return normalized.contains("password")
                || normalized.contains("token")
                || normalized.contains("authorization")
                || normalized.contains("secret")
                || normalized.contains("credential");
    }

    private static String abbreviate(String value, int maxLength) {
        if (value == null) {
            return "-";
        }
        String normalized = value.replaceAll("[\\r\\n\\t]+", " ").trim();
        if (normalized.length() <= maxLength) {
            return normalized;
        }
        return normalized.substring(0, maxLength - 3) + "...";
    }
}
