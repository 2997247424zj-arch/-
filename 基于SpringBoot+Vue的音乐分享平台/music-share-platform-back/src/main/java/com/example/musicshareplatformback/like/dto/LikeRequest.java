package com.example.musicshareplatformback.like.dto;

public record LikeRequest(
        String userId,
        String targetType,
        String targetId
) {
}
