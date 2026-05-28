package com.example.musicshareplatformback.playlist.service.impl;

import com.example.musicshareplatformback.community.dto.CommentSummary;
import com.example.musicshareplatformback.playlist.dto.PlaylistDetailResponse;
import com.example.musicshareplatformback.playlist.dto.PlaylistListResponse;
import com.example.musicshareplatformback.playlist.dto.PlaylistSummary;
import com.example.musicshareplatformback.playlist.dto.PlaylistTrackSummary;
import com.example.musicshareplatformback.playlist.service.PlaylistService;

import java.util.List;

public class StaticPlaylistService implements PlaylistService {

    private static final String SAMPLE_STREAM_URL = "https://samplefile.com/samples/download/audio/mp3/mp3_30s_sample_file_470KB.mp3/";

    @Override
    public PlaylistListResponse getPlaylists() {
        return new PlaylistListResponse(List.of(
                new PlaylistSummary("playlist-1", "深夜赶工不掉线", "Aurora", "学习专注", 28, 1640, "#ff8a5b", "适合写代码、剪片和整理灵感的稳定节奏歌单。"),
                new PlaylistSummary("playlist-2", "雨后开车回家", "Mori", "通勤驾驶", 21, 1288, "#3858ff", "偏晚归路程感的 city pop、轻电子和氛围人声。"),
                new PlaylistSummary("playlist-3", "小型现场精选", "Nora", "现场回放", 16, 942, "#9b51e0", "汇总独立音乐现场片段、彩排录音和氛围版改编。"),
                new PlaylistSummary("playlist-4", "周末房间广播", "Night Listener", "房间电台", 24, 806, "#6fcf97", "适合独处、收纳和阅读时段的温和播放队列。")
        ));
    }

    @Override
    public PlaylistDetailResponse getPlaylistDetail(String playlistId) {
        return new PlaylistDetailResponse(
                playlistId,
                "深夜赶工不掉线",
                "Aurora",
                "creator-1",
                "用于本地静态测试的策展人简介。",
                "学习专注",
                4,
                1640,
                1,
                "#ff8a5b",
                "用于本地静态测试的歌单详情。",
                List.of(
                        new PlaylistTrackSummary("song-1", "Midnight Run", "Kite Harbor", "Electronic", "03:42", 1298, SAMPLE_STREAM_URL, SAMPLE_STREAM_URL, true, "REMOTE")
                )
        );
    }

    @Override
    public List<CommentSummary> getPlaylistComments(String playlistId) {
        return List.of(
                new CommentSummary("comment-demo-playlist", "Demo User", "PLAYLIST", "深夜赶工不掉线", "这是一条静态歌单评论。", 1, "刚刚")
        );
    }
}
