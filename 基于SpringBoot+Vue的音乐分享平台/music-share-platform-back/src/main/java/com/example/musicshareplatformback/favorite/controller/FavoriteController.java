package com.example.musicshareplatformback.favorite.controller;

import com.example.musicshareplatformback.common.ApiResponse;
import com.example.musicshareplatformback.favorite.dto.FavoriteRequest;
import com.example.musicshareplatformback.favorite.dto.FavoriteResponse;
import com.example.musicshareplatformback.favorite.service.FavoriteService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/favorites")
public class FavoriteController {

    private final FavoriteService favoriteService;

    public FavoriteController(FavoriteService favoriteService) {
        this.favoriteService = favoriteService;
    }

    @PostMapping
    public ApiResponse<FavoriteResponse> createFavorite(@RequestBody FavoriteRequest request) {
        return ApiResponse.success("Favorite created.", favoriteService.createFavorite(request));
    }
}
