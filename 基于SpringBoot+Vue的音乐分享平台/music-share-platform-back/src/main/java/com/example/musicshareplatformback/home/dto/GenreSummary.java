package com.example.musicshareplatformback.home.dto;

public record GenreSummary(
        String id,
        String name,
        String description,
        String accentColor,
        int trackCount
) {
}
