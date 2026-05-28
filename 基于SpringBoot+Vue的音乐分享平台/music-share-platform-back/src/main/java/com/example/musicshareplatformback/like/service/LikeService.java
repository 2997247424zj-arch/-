package com.example.musicshareplatformback.like.service;

import com.example.musicshareplatformback.like.dto.LikeRequest;
import com.example.musicshareplatformback.like.dto.LikeResponse;

public interface LikeService {

    LikeResponse createLike(LikeRequest request);
}
