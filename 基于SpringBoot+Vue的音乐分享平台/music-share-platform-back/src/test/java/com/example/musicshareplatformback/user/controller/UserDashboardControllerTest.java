package com.example.musicshareplatformback.user.controller;

import com.example.musicshareplatformback.user.dto.RecentActionSummary;
import com.example.musicshareplatformback.user.dto.UserDashboardResponse;
import com.example.musicshareplatformback.user.service.UserDashboardService;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class UserDashboardControllerTest {

    @Test
    void shouldReturnUserDashboard() throws Exception {
        UserDashboardService service = userId -> new UserDashboardResponse(
                userId,
                "Night Listener",
                "USER",
                "Lo-fi",
                "个人简介",
                2,
                3,
                1,
                List.of(new RecentActionSummary("favorite-1", "SONG", "Noise Map", "最近加入收藏", "1 天前")),
                List.of(new RecentActionSummary("like-1", "SONG", "Noise Map", "最近点赞", "1 天前")),
                List.of(new RecentActionSummary("comment-1", "SONG", "Midnight Run", "评论内容", "刚刚"))
        );

        MockMvc mockMvc = MockMvcBuilders.standaloneSetup(new UserDashboardController(service)).build();
        mockMvc.perform(get("/api/users/user-1/dashboard"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.userId").value("user-1"))
                .andExpect(jsonPath("$.data.recentFavorites.length()").value(1));
    }
}
