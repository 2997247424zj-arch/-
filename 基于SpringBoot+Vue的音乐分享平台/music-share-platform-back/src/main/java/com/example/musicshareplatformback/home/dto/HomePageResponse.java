package com.example.musicshareplatformback.home.dto;

import java.util.List;

public record HomePageResponse(
        PlatformOverview overview,
        List<GenreSummary> genres,
        List<TrackSummary> trendingTracks,
        List<HomePlayableTrack> recommendedSleepTracks,
        List<PlaylistSummary> featuredPlaylists,
        List<ActivitySummary> latestActivities,
        List<String> deliveryRoadmap
) {
}
