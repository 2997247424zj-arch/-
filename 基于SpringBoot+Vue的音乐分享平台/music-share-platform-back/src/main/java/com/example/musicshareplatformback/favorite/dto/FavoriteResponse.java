package com.example.musicshareplatformback.favorite.dto;

public record FavoriteResponse(
        String targetType,
        String targetId,
        int totalFavorites
) {
}
