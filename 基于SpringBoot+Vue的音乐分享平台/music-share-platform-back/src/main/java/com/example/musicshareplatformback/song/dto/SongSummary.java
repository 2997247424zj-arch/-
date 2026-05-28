package com.example.musicshareplatformback.song.dto;

public record SongSummary(
        String id,
        String title,
        String artist,
        String genre,
        String moodTag,
        String durationText,
        int playCount,
        int likeCount,
        String highlightColor,
        String description,
        String streamUrl,
        String downloadUrl,
        boolean streamAvailable,
        String audioSourceType
) {
}
