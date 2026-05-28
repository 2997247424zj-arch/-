package com.example.musicshareplatformback.home.dto;

public record HomePlayableTrack(
        String id,
        String title,
        String artist,
        String genre,
        String moodTag,
        String durationText,
        String description,
        String highlightColor,
        String streamUrl,
        String downloadUrl,
        boolean streamAvailable,
        String audioSourceType
) {
}
