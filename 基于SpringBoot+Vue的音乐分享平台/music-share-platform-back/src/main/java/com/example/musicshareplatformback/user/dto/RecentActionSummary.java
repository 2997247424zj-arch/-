package com.example.musicshareplatformback.user.dto;

public record RecentActionSummary(
        String id,
        String type,
        String title,
        String subtitle,
        String relativeTime
) {
}
