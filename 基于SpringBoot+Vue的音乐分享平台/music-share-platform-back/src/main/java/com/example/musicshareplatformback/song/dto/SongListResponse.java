package com.example.musicshareplatformback.song.dto;

import java.util.List;

public record SongListResponse(
        List<String> availableGenres,
        List<SongSummary> songs
) {
}
