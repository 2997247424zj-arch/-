package com.example.musicshareplatformback.home.dto;

public record TrackSummary(
        String id,
        String title,
        String artist,
        String genre,
        String moodTag,
        String durationText,
        String description,
        String highlightColor,
        int likes,
        String streamUrl,
        String downloadUrl,
        boolean streamAvailable,
        String audioSourceType
) {
}
