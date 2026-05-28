package com.example.musicshareplatformback.community.dto;

import com.example.musicshareplatformback.home.dto.ActivitySummary;

import java.util.List;

public record CommunityResponse(
        int totalComments,
        int totalPlaylists,
        int totalCreators,
        List<ActivitySummary> latestActivities,
        List<CommentSummary> latestComments
) {
}
