package com.example.musicshareplatformback.playlist.controller;

import com.example.musicshareplatformback.playlist.service.PlaylistService;
import com.example.musicshareplatformback.playlist.service.impl.StaticPlaylistService;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class PlaylistControllerTest {

    @Test
    void shouldReturnPlaylists() throws Exception {
        PlaylistService playlistService = new StaticPlaylistService();
        MockMvc mockMvc = MockMvcBuilders.standaloneSetup(new PlaylistController(playlistService)).build();

        mockMvc.perform(get("/api/playlists"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.playlists.length()").value(4));
    }
}
