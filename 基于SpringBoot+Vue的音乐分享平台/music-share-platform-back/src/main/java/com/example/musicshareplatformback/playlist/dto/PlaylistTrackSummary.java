package com.example.musicshareplatformback.playlist.dto;

public record PlaylistTrackSummary(
        String id,
        String title,
        String artist,
        String genre,
        String durationText,
        int likeCount,
        String streamUrl,
        String downloadUrl,
        boolean streamAvailable,
        String audioSourceType
) {
}
