package com.example.musicshareplatformback.playlist.service;

import com.example.musicshareplatformback.community.dto.CommentSummary;
import com.example.musicshareplatformback.playlist.dto.PlaylistDetailResponse;
import com.example.musicshareplatformback.playlist.dto.PlaylistListResponse;

import java.util.List;

public interface PlaylistService {

    PlaylistListResponse getPlaylists();

    PlaylistDetailResponse getPlaylistDetail(String playlistId);

    List<CommentSummary> getPlaylistComments(String playlistId);
}
