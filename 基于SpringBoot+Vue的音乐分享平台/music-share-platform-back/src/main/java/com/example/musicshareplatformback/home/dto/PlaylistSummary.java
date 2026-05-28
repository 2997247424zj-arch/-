package com.example.musicshareplatformback.home.dto;

public record PlaylistSummary(
        String id,
        String title,
        String curator,
        String description,
        int trackCount,
        int followers
) {
}
