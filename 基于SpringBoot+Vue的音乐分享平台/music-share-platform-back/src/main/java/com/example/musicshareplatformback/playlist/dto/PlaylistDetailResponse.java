package com.example.musicshareplatformback.playlist.dto;

import java.util.List;

public record PlaylistDetailResponse(
        String id,
        String title,
        String curator,
        String curatorUserId,
        String curatorBio,
        String sceneTag,
        int trackCount,
        int followers,
        int commentCount,
        String coverColor,
        String description,
        List<PlaylistTrackSummary> songs
) {
}
