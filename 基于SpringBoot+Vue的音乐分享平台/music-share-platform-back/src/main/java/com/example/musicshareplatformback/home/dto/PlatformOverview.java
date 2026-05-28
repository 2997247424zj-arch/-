package com.example.musicshareplatformback.home.dto;

public record PlatformOverview(
        int totalTracks,
        int totalPlaylists,
        int activeCreators,
        int dailyShares
) {
}
