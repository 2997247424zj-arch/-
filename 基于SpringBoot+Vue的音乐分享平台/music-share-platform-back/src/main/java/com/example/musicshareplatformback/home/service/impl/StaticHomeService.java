package com.example.musicshareplatformback.home.service.impl;

import com.example.musicshareplatformback.home.dto.ActivitySummary;
import com.example.musicshareplatformback.home.dto.GenreSummary;
import com.example.musicshareplatformback.home.dto.HomePageResponse;
import com.example.musicshareplatformback.home.dto.HomePlayableTrack;
import com.example.musicshareplatformback.home.dto.PlatformOverview;
import com.example.musicshareplatformback.home.dto.PlaylistSummary;
import com.example.musicshareplatformback.home.dto.TrackSummary;
import com.example.musicshareplatformback.home.service.HomeService;

import java.util.List;

public class StaticHomeService implements HomeService {

    private static final String SAMPLE_STREAM_URL = "https://samplefile.com/samples/download/audio/mp3/mp3_30s_sample_file_470KB.mp3/";

    @Override
    public HomePageResponse getHomePage() {
        return new HomePageResponse(
                new PlatformOverview(15, 4, 3, 4),
                List.of(
                        new GenreSummary("genre-city-pop", "City Pop", "适合通勤、夜归和轻复古氛围。", "#ff8a5b", 4),
                        new GenreSummary("genre-indie", "Indie", "适合深夜慢听和更轻的人声流行。", "#4f7cff", 4),
                        new GenreSummary("genre-lofi", "Lo-fi", "低保真节拍更适合夜间安静循环。", "#6fcf97", 4),
                        new GenreSummary("genre-electronic", "Electronic", "适合氛围电子和空间感延展。", "#9b51e0", 3)
                ),
                List.of(
                        new TrackSummary("song-1", "Midnight Run", "Kite Harbor", "Electronic", "夜行感", "03:42", "适合夜跑、车窗反光和加速感画面的电子单曲。", "#3858ff", 1298, SAMPLE_STREAM_URL, SAMPLE_STREAM_URL, true, "REMOTE"),
                        new TrackSummary("song-2", "Paper Sun", "Luna Echo", "Indie", "清晨通透", "04:08", "带一点木吉他和轻拍节奏的独立流行作品。", "#ffb347", 1084, SAMPLE_STREAM_URL, SAMPLE_STREAM_URL, true, "REMOTE"),
                        new TrackSummary("song-3", "Noise Map", "Blue Archive", "Lo-fi", "专注循环", "02:58", "适合学习和长时间专注的低保真循环节拍。", "#6fcf97", 967, SAMPLE_STREAM_URL, SAMPLE_STREAM_URL, true, "REMOTE"),
                        new TrackSummary("song-4", "Ocean Drive Demo", "Rooftop Cinema", "City Pop", "海风复古", "03:36", "复古合成器与轻人声结合的 city pop demo。", "#ff8a5b", 893, SAMPLE_STREAM_URL, SAMPLE_STREAM_URL, true, "REMOTE")
                ),
                List.of(
                        new HomePlayableTrack("song-10", "Sleepless Afterglow", "Aurora Lane", "Indie", "失眠安抚", "01:00", "适合夜间难以入睡时降低情绪波动的轻流行段落。", "#8aa1ff", "https://samplefile.com/samples/download/audio/mp3/mp3_60s_sample_file_939KB.mp3/", "https://samplefile.com/samples/download/audio/mp3/mp3_60s_sample_file_939KB.mp3/", true, "REMOTE"),
                        new HomePlayableTrack("song-11", "Pillow Talk Lights", "Mori", "City Pop", "夜色轻拍", "00:30", "适合熄灯后低音量循环的柔和 city pop。", "#ff9bb0", "https://samplefile.com/samples/download/audio/mp3/mp3_30s_sample_file_470KB.mp3/", "https://samplefile.com/samples/download/audio/mp3/mp3_30s_sample_file_470KB.mp3/", true, "REMOTE")
                ),
                List.of(
                        new PlaylistSummary("playlist-1", "深夜赶工不掉线", "Aurora", "适合写代码、剪片和整理灵感的稳定节奏歌单。", 4, 1640),
                        new PlaylistSummary("playlist-2", "雨后开车回家", "Mori", "偏晚归路程感的 city pop、轻电子和氛围人声。", 4, 1288),
                        new PlaylistSummary("playlist-3", "小型现场精选", "Nora", "汇总独立音乐现场片段、彩排录音和氛围版改编。", 3, 942)
                ),
                List.of(
                        new ActivitySummary("activity-1", "Mina", "分享了歌单", "深夜赶工不掉线", "3 分钟前"),
                        new ActivitySummary("activity-2", "Aven", "发布了评论", "Midnight Run", "12 分钟前"),
                        new ActivitySummary("activity-3", "Sora", "收藏了歌曲", "Paper Sun", "26 分钟前"),
                        new ActivitySummary("activity-4", "EchoLab", "上传了新 demo", "Ocean Drive Demo", "1 小时前")
                ),
                List.of(
                        "第 1 周：完成用户、歌曲、歌单、评论四个核心实体建模。",
                        "第 2 周：接入数据库与鉴权，补充上传、点赞、收藏能力。",
                        "第 3 周：完成发现页、个人音乐库、社区讨论与后台管理。"
                )
        );
    }
}
