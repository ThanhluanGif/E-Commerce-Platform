package com.ecommerce.common.util;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

public class DateTimeUtils {
    private static final String DEFAULT_PATTERN = "yyyy-MM-dd HH:mm:ss";
    private static final ZoneId DEFAULT_ZONE_ID = ZoneId.of("Asia/Ho_Chi_Minh");

    public static String formatInstant(Instant instant) {
        if (instant == null) return null;
        return DateTimeFormatter.ofPattern(DEFAULT_PATTERN)
                .withZone(DEFAULT_ZONE_ID)
                .format(instant);
    }

    public static String formatInstant(Instant instant, String pattern) {
        if (instant == null || pattern == null) return null;
        return DateTimeFormatter.ofPattern(pattern)
                .withZone(DEFAULT_ZONE_ID)
                .format(instant);
    }

    public static Instant parseToInstant(String dateTimeStr) {
        if (dateTimeStr == null || dateTimeStr.isEmpty()) return null;
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(DEFAULT_PATTERN)
                .withZone(DEFAULT_ZONE_ID);
        return Instant.from(formatter.parse(dateTimeStr));
    }

    public static Instant parseToInstant(String dateTimeStr, String pattern) {
        if (dateTimeStr == null || dateTimeStr.isEmpty() || pattern == null) return null;
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(pattern)
                .withZone(DEFAULT_ZONE_ID);
        return Instant.from(formatter.parse(dateTimeStr));
    }

    public static LocalDateTime toLocalDateTime(Instant instant) {
        if (instant == null) return null;
        return LocalDateTime.ofInstant(instant, DEFAULT_ZONE_ID);
    }
}
