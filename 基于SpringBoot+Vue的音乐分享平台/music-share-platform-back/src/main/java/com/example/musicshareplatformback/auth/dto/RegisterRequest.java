package com.example.musicshareplatformback.auth.dto;

public record RegisterRequest(
        String username,
        String password,
        String displayName,
        String role,
        String favoriteGenre,
        String bio
) {
}
