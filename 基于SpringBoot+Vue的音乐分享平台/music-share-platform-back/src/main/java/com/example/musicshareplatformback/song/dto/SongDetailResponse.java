package com.example.musicshareplatformback.song.dto;

import java.util.List;

public record SongDetailResponse(
        String id,
        String title,
        String artist,
        String genre,
        String moodTag,
        String durationText,
        int playCount,
        int likeCount,
        int favoriteCount,
        int commentCount,
        String highlightColor,
        String description,
        String audioUrl,
        String streamUrl,
        String downloadUrl,
        boolean streamAvailable,
        String audioSourceType,
        String coverUrl,
        String uploaderName,
        String uploaderBio,
        List<String> tags
) {
}
