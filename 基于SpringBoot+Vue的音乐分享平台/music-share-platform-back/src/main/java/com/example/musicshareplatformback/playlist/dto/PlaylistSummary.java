package com.example.musicshareplatformback.playlist.dto;

public record PlaylistSummary(
        String id,
        String title,
        String curator,
        String sceneTag,
        int trackCount,
        int followers,
        String coverColor,
        String description
) {
}
