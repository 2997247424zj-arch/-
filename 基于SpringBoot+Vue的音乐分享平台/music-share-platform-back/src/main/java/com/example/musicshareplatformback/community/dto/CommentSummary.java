package com.example.musicshareplatformback.community.dto;

public record CommentSummary(
        String id,
        String userName,
        String targetType,
        String targetName,
        String content,
        int likeCount,
        String relativeTime
) {
}
