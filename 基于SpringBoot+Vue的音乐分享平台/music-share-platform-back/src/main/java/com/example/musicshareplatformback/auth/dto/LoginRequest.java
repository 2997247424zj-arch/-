package com.example.musicshareplatformback.auth.dto;

public record LoginRequest(
        String username,
        String password
) {
}
