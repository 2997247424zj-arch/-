package com.example.musicshareplatformback.song.dto;

public record CreateSongResponse(
        String songId,
        String title,
        String audioUrl,
        String streamUrl,
        String downloadUrl,
        String audioSourceType
) {
}
