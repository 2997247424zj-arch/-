package com.example.musicshareplatformback.comment.controller;

import com.example.musicshareplatformback.comment.dto.CreateCommentRequest;
import com.example.musicshareplatformback.comment.service.CommentService;
import com.example.musicshareplatformback.community.dto.CommentSummary;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class CommentControllerTest {

    @Test
    void shouldCreateComment() throws Exception {
        CommentService commentService = request -> new CommentSummary(
                "comment-new",
                "Night Listener",
                "SONG",
                "Midnight Run",
                request.content(),
                0,
                "刚刚"
        );

        MockMvc mockMvc = MockMvcBuilders.standaloneSetup(new CommentController(commentService)).build();

        mockMvc.perform(post("/api/comments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "userId": "user-1",
                                  "songId": "song-1",
                                  "content": "评论测试"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content").value("评论测试"));
    }
}
