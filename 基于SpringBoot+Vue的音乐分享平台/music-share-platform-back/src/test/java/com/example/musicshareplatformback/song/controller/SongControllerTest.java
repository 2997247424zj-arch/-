package com.example.musicshareplatformback.song.controller;

import com.example.musicshareplatformback.song.service.SongService;
import com.example.musicshareplatformback.song.service.impl.StaticSongService;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class SongControllerTest {

    @Test
    void shouldFilterSongsByGenre() throws Exception {
        SongService songService = new StaticSongService();
        MockMvc mockMvc = MockMvcBuilders.standaloneSetup(new SongController(songService)).build();

        mockMvc.perform(get("/api/songs").param("genre", "Lo-fi"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.songs.length()").value(2))
                .andExpect(jsonPath("$.data.songs[0].genre").value("Lo-fi"));
    }
}
