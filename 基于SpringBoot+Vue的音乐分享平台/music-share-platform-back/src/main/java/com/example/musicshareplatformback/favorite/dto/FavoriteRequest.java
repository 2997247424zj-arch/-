package com.example.musicshareplatformback.favorite.dto;

public record FavoriteRequest(
        String userId,
        String targetType,
        String targetId
) {
}
