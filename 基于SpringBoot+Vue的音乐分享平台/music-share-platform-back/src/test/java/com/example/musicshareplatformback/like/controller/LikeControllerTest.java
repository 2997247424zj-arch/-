package com.example.musicshareplatformback.like.controller;

import com.example.musicshareplatformback.like.dto.LikeResponse;
import com.example.musicshareplatformback.like.service.LikeService;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class LikeControllerTest {

    @Test
    void shouldCreateLike() throws Exception {
        LikeService likeService = request -> new LikeResponse(request.targetType(), request.targetId(), 9);
        MockMvc mockMvc = MockMvcBuilders.standaloneSetup(new LikeController(likeService)).build();

        mockMvc.perform(post("/api/likes")
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
                .andExpect(jsonPath("$.data.totalLikes").value(9));
    }
}
