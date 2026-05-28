package com.example.musicshareplatformback.community.controller;

import com.example.musicshareplatformback.common.ApiResponse;
import com.example.musicshareplatformback.community.dto.CommunityResponse;
import com.example.musicshareplatformback.community.service.CommunityService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/community")
public class CommunityController {

    private final CommunityService communityService;

    public CommunityController(CommunityService communityService) {
        this.communityService = communityService;
    }

    @GetMapping
    public ApiResponse<CommunityResponse> getCommunitySnapshot() {
        return ApiResponse.success("Community snapshot loaded.", communityService.getCommunitySnapshot());
    }
}
