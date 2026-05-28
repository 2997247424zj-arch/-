package com.example.musicshareplatformback.home.dto;

public record ActivitySummary(
        String id,
        String userName,
        String action,
        String targetName,
        String relativeTime
) {
}
