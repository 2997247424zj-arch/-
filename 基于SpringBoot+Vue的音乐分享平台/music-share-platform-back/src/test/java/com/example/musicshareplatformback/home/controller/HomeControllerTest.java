package com.example.musicshareplatformback.home.controller;

import com.example.musicshareplatformback.home.service.HomeService;
import com.example.musicshareplatformback.home.service.impl.StaticHomeService;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class HomeControllerTest {

    @Test
    void shouldReturnHomepageData() throws Exception {
        HomeService homeService = new StaticHomeService();
        MockMvc mockMvc = MockMvcBuilders.standaloneSetup(new HomeController(homeService)).build();

        mockMvc.perform(get("/api/home"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.overview.totalTracks").value(15))
                .andExpect(jsonPath("$.data.trendingTracks.length()").value(4))
                .andExpect(jsonPath("$.data.recommendedSleepTracks.length()").value(2))
                .andExpect(jsonPath("$.data.featuredPlaylists.length()").value(3));
    }
}
