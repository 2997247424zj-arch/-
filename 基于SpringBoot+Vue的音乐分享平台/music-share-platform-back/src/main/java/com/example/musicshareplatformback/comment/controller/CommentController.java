package com.example.musicshareplatformback.comment.controller;

import com.example.musicshareplatformback.comment.dto.CreateCommentRequest;
import com.example.musicshareplatformback.comment.service.CommentService;
import com.example.musicshareplatformback.common.ApiResponse;
import com.example.musicshareplatformback.community.dto.CommentSummary;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/comments")
public class CommentController {

    private final CommentService commentService;

    public CommentController(CommentService commentService) {
        this.commentService = commentService;
    }

    @PostMapping
    public ApiResponse<CommentSummary> createComment(@RequestBody CreateCommentRequest request) {
        return ApiResponse.success("Comment created.", commentService.createComment(request));
    }
}
