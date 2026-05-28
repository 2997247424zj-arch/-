package com.example.musicshareplatformback.song.service;

import com.example.musicshareplatformback.community.dto.CommentSummary;
import com.example.musicshareplatformback.song.dto.CreateSongRequest;
import com.example.musicshareplatformback.song.dto.CreateSongResponse;
import com.example.musicshareplatformback.song.dto.SongDetailResponse;
import com.example.musicshareplatformback.song.dto.SongListResponse;

import java.util.List;

public interface SongService {

    SongListResponse getSongs(String keyword, String genre);

    SongDetailResponse getSongDetail(String songId);

    List<CommentSummary> getSongComments(String songId);

    CreateSongResponse createSong(CreateSongRequest request);
}
