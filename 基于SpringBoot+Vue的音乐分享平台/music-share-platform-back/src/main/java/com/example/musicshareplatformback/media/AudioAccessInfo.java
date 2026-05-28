package com.example.musicshareplatformback.media;

public record AudioAccessInfo(
        String originalUrl,
        String streamUrl,
        String downloadUrl,
        boolean available,
        String sourceType
) {
}
