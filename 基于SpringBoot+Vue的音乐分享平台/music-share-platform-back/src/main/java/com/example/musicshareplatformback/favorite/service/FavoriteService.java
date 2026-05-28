package com.example.musicshareplatformback.favorite.service;

import com.example.musicshareplatformback.favorite.dto.FavoriteRequest;
import com.example.musicshareplatformback.favorite.dto.FavoriteResponse;

public interface FavoriteService {

    FavoriteResponse createFavorite(FavoriteRequest request);
}
