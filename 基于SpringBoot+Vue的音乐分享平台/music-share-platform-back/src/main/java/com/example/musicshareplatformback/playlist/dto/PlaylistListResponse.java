package com.example.musicshareplatformback.playlist.dto;

import java.util.List;

public record PlaylistListResponse(
        List<PlaylistSummary> playlists
) {
}
