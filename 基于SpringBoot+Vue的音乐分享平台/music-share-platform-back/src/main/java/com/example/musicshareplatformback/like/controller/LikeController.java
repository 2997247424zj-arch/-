package com.example.musicshareplatformback.like.controller;

import com.example.musicshareplatformback.common.ApiResponse;
import com.example.musicshareplatformback.like.dto.LikeRequest;
import com.example.musicshareplatformback.like.dto.LikeResponse;
import com.example.musicshareplatformback.like.service.LikeService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/likes")
public class LikeController {

    private final LikeService likeService;

    public LikeController(LikeService likeService) {
        this.likeService = likeService;
    }

    @PostMapping
    public ApiResponse<LikeResponse> createLike(@RequestBody LikeRequest request) {
        return ApiResponse.success("Like created.", likeService.createLike(request));
    }
}
