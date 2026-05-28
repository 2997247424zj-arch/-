package com.example.musicshareplatformback.community.controller;

import com.example.musicshareplatformback.community.dto.CommentSummary;
import com.example.musicshareplatformback.community.dto.CommunityResponse;
import com.example.musicshareplatformback.community.service.CommunityService;
import com.example.musicshareplatformback.home.dto.ActivitySummary;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class CommunityControllerTest {

    @Test
    void shouldReturnCommunitySnapshot() throws Exception {
        CommunityService communityService = () -> new CommunityResponse(
                6,
                4,
                3,
                List.of(new ActivitySummary("activity-1", "Mina", "分享了歌单", "深夜赶工不掉线", "3 分钟前")),
                List.of(new CommentSummary("comment-1", "Aven", "SONG", "Midnight Run", "这个鼓点特别适合夜跑。", 16, "12 分钟前"))
        );

        MockMvc mockMvc = MockMvcBuilders.standaloneSetup(new CommunityController(communityService)).build();

        mockMvc.perform(get("/api/community"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.totalComments").value(6))
                .andExpect(jsonPath("$.data.latestComments.length()").value(1));
    }
}
