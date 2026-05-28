package com.example.musicshareplatformback.playlist.controller;

import com.example.musicshareplatformback.common.ApiResponse;
import com.example.musicshareplatformback.community.dto.CommentSummary;
import com.example.musicshareplatformback.playlist.dto.PlaylistDetailResponse;
import com.example.musicshareplatformback.playlist.dto.PlaylistListResponse;
import com.example.musicshareplatformback.playlist.service.PlaylistService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/playlists")
public class PlaylistController {

    private final PlaylistService playlistService;

    public PlaylistController(PlaylistService playlistService) {
        this.playlistService = playlistService;
    }

    @GetMapping
    public ApiResponse<PlaylistListResponse> getPlaylists() {
        return ApiResponse.success("Playlist list loaded.", playlistService.getPlaylists());
    }

    @GetMapping("/{playlistId}")
    public ApiResponse<PlaylistDetailResponse> getPlaylistDetail(@PathVariable String playlistId) {
        return ApiResponse.success("Playlist detail loaded.", playlistService.getPlaylistDetail(playlistId));
    }

    @GetMapping("/{playlistId}/comments")
    public ApiResponse<List<CommentSummary>> getPlaylistComments(@PathVariable String playlistId) {
        return ApiResponse.success("Playlist comments loaded.", playlistService.getPlaylistComments(playlistId));
    }
}
