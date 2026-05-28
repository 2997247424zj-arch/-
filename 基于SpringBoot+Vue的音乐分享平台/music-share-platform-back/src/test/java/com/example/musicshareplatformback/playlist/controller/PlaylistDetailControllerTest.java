package com.example.musicshareplatformback.playlist.controller;

import com.example.musicshareplatformback.community.dto.CommentSummary;
import com.example.musicshareplatformback.playlist.dto.PlaylistDetailResponse;
import com.example.musicshareplatformback.playlist.dto.PlaylistListResponse;
import com.example.musicshareplatformback.playlist.dto.PlaylistSummary;
import com.example.musicshareplatformback.playlist.dto.PlaylistTrackSummary;
import com.example.musicshareplatformback.playlist.service.PlaylistService;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class PlaylistDetailControllerTest {

    @Test
    void shouldReturnPlaylistDetail() throws Exception {
        PlaylistService playlistService = new PlaylistService() {
            @Override
            public PlaylistListResponse getPlaylists() {
                return new PlaylistListResponse(List.of());
            }

            @Override
            public PlaylistDetailResponse getPlaylistDetail(String playlistId) {
                return new PlaylistDetailResponse(
                        playlistId,
                        "深夜赶工不掉线",
                        "Aurora",
                        "creator-1",
                        "创作者简介",
                        "学习专注",
                        4,
                        1640,
                        1,
                        "#ff8a5b",
                        "歌单详情",
                        List.of(new PlaylistTrackSummary("song-1", "Midnight Run", "Kite Harbor", "Electronic", "03:42", 1298, "https://example.com/song.mp3", "https://example.com/song.mp3", true, "REMOTE"))
                );
            }

            @Override
            public List<CommentSummary> getPlaylistComments(String playlistId) {
                return List.of(new CommentSummary("comment-1", "Night Listener", "PLAYLIST", "深夜赶工不掉线", "不错", 1, "刚刚"));
            }
        };

        MockMvc mockMvc = MockMvcBuilders.standaloneSetup(new PlaylistController(playlistService)).build();
        mockMvc.perform(get("/api/playlists/playlist-1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value("playlist-1"))
                .andExpect(jsonPath("$.data.songs.length()").value(1));
    }
}
