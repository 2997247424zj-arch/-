package com.example.musicshareplatformback.auth.dto;

public record UserProfile(
        String id,
        String username,
        String displayName,
        String role,
        String favoriteGenre,
        String bio
) {
}
