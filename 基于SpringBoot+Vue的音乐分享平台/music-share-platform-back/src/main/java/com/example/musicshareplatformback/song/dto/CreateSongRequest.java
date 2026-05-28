package com.example.musicshareplatformback.song.dto;

import java.util.List;

public record CreateSongRequest(
        String userId,
        String title,
        String artist,
        String genre,
        String moodTag,
        String durationText,
        String description,
        String audioUrl,
        String coverUrl,
        List<String> tags
) {
}
