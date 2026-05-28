package com.example.musicshareplatformback.song.service.impl;

import com.example.musicshareplatformback.community.dto.CommentSummary;
import com.example.musicshareplatformback.song.dto.CreateSongRequest;
import com.example.musicshareplatformback.song.dto.CreateSongResponse;
import com.example.musicshareplatformback.song.dto.SongDetailResponse;
import com.example.musicshareplatformback.song.dto.SongListResponse;
import com.example.musicshareplatformback.song.dto.SongSummary;
import com.example.musicshareplatformback.song.service.SongService;

import java.util.List;
import java.util.Locale;

public class StaticSongService implements SongService {

    private static final List<String> AVAILABLE_GENRES = List.of("All", "City Pop", "Electronic", "Indie", "Lo-fi");
    private static final String SAMPLE_STREAM_URL = "https://samplefile.com/samples/download/audio/mp3/mp3_30s_sample_file_470KB.mp3/";

    private static final List<SongSummary> SONGS = List.of(
            new SongSummary("song-1", "Midnight Run", "Kite Harbor", "Electronic", "夜行感", "03:42", 18240, 1298, "#3858ff", "适合夜跑、车窗反光和加速感画面的电子单曲。", SAMPLE_STREAM_URL, SAMPLE_STREAM_URL, true, "REMOTE"),
            new SongSummary("song-2", "Paper Sun", "Luna Echo", "Indie", "清晨通透", "04:08", 15420, 1084, "#ffb347", "带一点木吉他和轻拍节奏的独立流行作品。", SAMPLE_STREAM_URL, SAMPLE_STREAM_URL, true, "REMOTE"),
            new SongSummary("song-3", "Noise Map", "Blue Archive", "Lo-fi", "专注循环", "02:58", 12680, 967, "#6fcf97", "适合学习和长时间专注的低保真循环节拍。", SAMPLE_STREAM_URL, SAMPLE_STREAM_URL, true, "REMOTE"),
            new SongSummary("song-4", "Ocean Drive Demo", "Rooftop Cinema", "City Pop", "海风复古", "03:36", 11790, 893, "#ff8a5b", "复古合成器与轻人声结合的 city pop demo。", SAMPLE_STREAM_URL, SAMPLE_STREAM_URL, true, "REMOTE"),
            new SongSummary("song-5", "Static Bloom", "Afterglow", "Electronic", "空间扩散", "04:21", 9840, 756, "#9b51e0", "更偏氛围电子的扩散型编排，适合耳机收听。", SAMPLE_STREAM_URL, SAMPLE_STREAM_URL, true, "REMOTE"),
            new SongSummary("song-6", "Late Homework Club", "Cocoa Tape", "Lo-fi", "书桌夜灯", "02:44", 8620, 648, "#c17dff", "偏温暖的 lo-fi 鼓组和磁带噪点底色。", SAMPLE_STREAM_URL, SAMPLE_STREAM_URL, true, "REMOTE")
    );

    @Override
    public SongListResponse getSongs(String keyword, String genre) {
        String normalizedKeyword = normalize(keyword);
        String normalizedGenre = normalize(genre);

        List<SongSummary> filteredSongs = SONGS.stream()
                .filter(song -> matchesKeyword(song, normalizedKeyword))
                .filter(song -> matchesGenre(song, normalizedGenre))
                .toList();

        return new SongListResponse(AVAILABLE_GENRES, filteredSongs);
    }

    @Override
    public SongDetailResponse getSongDetail(String songId) {
        SongSummary song = SONGS.stream()
                .filter(item -> item.id().equals(songId))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("目标歌曲不存在。"));

        return new SongDetailResponse(
                song.id(),
                song.title(),
                song.artist(),
                song.genre(),
                song.moodTag(),
                song.durationText(),
                song.playCount(),
                song.likeCount(),
                0,
                0,
                song.highlightColor(),
                song.description(),
                song.streamUrl(),
                song.streamUrl(),
                song.downloadUrl(),
                song.streamAvailable(),
                song.audioSourceType(),
                "/media/covers/" + song.id() + ".jpg",
                "Demo Creator",
                "用于本地静态测试的创作者简介。",
                List.of(song.genre(), song.moodTag())
        );
    }

    @Override
    public List<CommentSummary> getSongComments(String songId) {
        return List.of(
                new CommentSummary("comment-demo", "Demo User", "SONG", "Midnight Run", "这是一条静态测试评论。", 1, "刚刚")
        );
    }

    @Override
    public CreateSongResponse createSong(CreateSongRequest request) {
        return new CreateSongResponse(
                "song-demo-created",
                request.title(),
                request.audioUrl(),
                request.audioUrl(),
                request.audioUrl(),
                request.audioUrl().startsWith("http") ? "REMOTE" : "LOCAL"
        );
    }

    private boolean matchesKeyword(SongSummary song, String keyword) {
        if (keyword.isBlank()) {
            return true;
        }

        String haystack = String.join(" ",
                song.title(),
                song.artist(),
                song.genre(),
                song.moodTag(),
                song.description()
        ).toLowerCase(Locale.ROOT);

        return haystack.contains(keyword);
    }

    private boolean matchesGenre(SongSummary song, String genre) {
        return genre.isBlank()
                || "all".equals(genre)
                || song.genre().toLowerCase(Locale.ROOT).equals(genre);
    }

    private String normalize(String value) {
        return value == null ? "" : value.trim().toLowerCase(Locale.ROOT);
    }
}
