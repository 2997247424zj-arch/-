package com.example.musicshareplatformback.favorite.controller;

import com.example.musicshareplatformback.favorite.dto.FavoriteRequest;
import com.example.musicshareplatformback.favorite.dto.FavoriteResponse;
import com.example.musicshareplatformback.favorite.service.FavoriteService;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class FavoriteControllerTest {

    @Test
    void shouldCreateFavorite() throws Exception {
        FavoriteService favoriteService = request -> new FavoriteResponse(request.targetType(), request.targetId(), 3);
        MockMvc mockMvc = MockMvcBuilders.standaloneSetup(new FavoriteController(favoriteService)).build();

        mockMvc.perform(post("/api/favorites")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "userId": "user-1",
                                  "targetType": "SONG",
                                  "targetId": "song-1"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.totalFavorites").value(3));
    }
}
