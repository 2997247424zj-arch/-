package com.example.musicshareplatformback.support;

import java.time.Duration;
import java.time.LocalDateTime;

public final class RelativeTimeFormatter {

    private RelativeTimeFormatter() {
    }

    public static String format(LocalDateTime createdAt) {
        Duration duration = Duration.between(createdAt, LocalDateTime.now());
        long minutes = Math.max(duration.toMinutes(), 0);

        if (minutes < 1) {
            return "刚刚";
        }
        if (minutes < 60) {
            return minutes + " 分钟前";
        }

        long hours = duration.toHours();
        if (hours < 24) {
            return hours + " 小时前";
        }

        long days = duration.toDays();
        return days + " 天前";
    }
}
