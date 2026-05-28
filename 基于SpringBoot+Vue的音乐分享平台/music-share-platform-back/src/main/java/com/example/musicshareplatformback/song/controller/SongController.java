package com.example.musicshareplatformback.song.controller;

import com.example.musicshareplatformback.common.ApiResponse;
import com.example.musicshareplatformback.community.dto.CommentSummary;
import com.example.musicshareplatformback.song.dto.CreateSongRequest;
import com.example.musicshareplatformback.song.dto.CreateSongResponse;
import com.example.musicshareplatformback.song.dto.SongDetailResponse;
import com.example.musicshareplatformback.song.dto.SongListResponse;
import com.example.musicshareplatformback.song.service.SongService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/songs")
public class SongController {

    private final SongService songService;

    public SongController(SongService songService) {
        this.songService = songService;
    }

    @GetMapping
    public ApiResponse<SongListResponse> getSongs(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String genre
    ) {
        return ApiResponse.success("Song list loaded.", songService.getSongs(keyword, genre));
    }

    @GetMapping("/{songId}")
    public ApiResponse<SongDetailResponse> getSongDetail(@PathVariable String songId) {
        return ApiResponse.success("Song detail loaded.", songService.getSongDetail(songId));
    }

    @GetMapping("/{songId}/comments")
    public ApiResponse<List<CommentSummary>> getSongComments(@PathVariable String songId) {
        return ApiResponse.success("Song comments loaded.", songService.getSongComments(songId));
    }

    @PostMapping
    public ApiResponse<CreateSongResponse> createSong(@RequestBody CreateSongRequest request) {
        return ApiResponse.success("Song created.", songService.createSong(request));
    }
}
