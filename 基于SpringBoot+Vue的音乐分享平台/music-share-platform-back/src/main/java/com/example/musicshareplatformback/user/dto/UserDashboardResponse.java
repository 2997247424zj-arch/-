package com.example.musicshareplatformback.user.dto;

import java.util.List;

public record UserDashboardResponse(
        String userId,
        String displayName,
        String role,
        String favoriteGenre,
        String bio,
        int totalFavorites,
        int totalLikes,
        int totalComments,
        List<RecentActionSummary> recentFavorites,
        List<RecentActionSummary> recentLikes,
        List<RecentActionSummary> recentComments
) {
}
