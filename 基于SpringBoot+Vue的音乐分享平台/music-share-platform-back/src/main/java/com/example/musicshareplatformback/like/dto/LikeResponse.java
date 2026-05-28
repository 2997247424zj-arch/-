package com.example.musicshareplatformback.like.dto;

public record LikeResponse(
        String targetType,
        String targetId,
        int totalLikes
) {
}
