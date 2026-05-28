package com.example.musicshareplatformback.media.dto;

public record UploadAudioResponse(
        String audioUrl,
        String streamUrl,
        String downloadUrl,
        String fileName,
        String sourceType
) {
}
