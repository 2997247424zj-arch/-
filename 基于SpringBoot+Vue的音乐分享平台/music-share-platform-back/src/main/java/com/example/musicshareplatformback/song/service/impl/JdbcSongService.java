package com.example.musicshareplatformback.song.service.impl;

import com.example.musicshareplatformback.community.dto.CommentSummary;
import com.example.musicshareplatformback.media.AudioAccessInfo;
import com.example.musicshareplatformback.media.AudioAccessResolver;
import com.example.musicshareplatformback.song.dto.CreateSongRequest;
import com.example.musicshareplatformback.song.dto.CreateSongResponse;
import com.example.musicshareplatformback.song.dto.SongDetailResponse;
import com.example.musicshareplatformback.song.dto.SongListResponse;
import com.example.musicshareplatformback.song.dto.SongSummary;
import com.example.musicshareplatformback.song.service.SongService;
import com.example.musicshareplatformback.support.PresentationContentOverrides;
import com.example.musicshareplatformback.support.RelativeTimeFormatter;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class JdbcSongService implements SongService {

    private final JdbcTemplate jdbcTemplate;
    private final AudioAccessResolver audioAccessResolver;

    public JdbcSongService(JdbcTemplate jdbcTemplate, AudioAccessResolver audioAccessResolver) {
        this.jdbcTemplate = jdbcTemplate;
        this.audioAccessResolver = audioAccessResolver;
    }

    @Override
    public SongListResponse getSongs(String keyword, String genre) {
        String normalizedKeyword = keyword == null ? "" : keyword.trim();
        String normalizedGenre = genre == null ? "" : genre.trim();

        List<String> genres = new ArrayList<>();
        genres.add("All");
        genres.addAll(jdbcTemplate.query("""
                        select distinct genre_name
                        from songs
                        where release_status = 'PUBLISHED'
                        order by genre_name
                        """,
                (rs, rowNum) -> rs.getString("genre_name")
        ));

        List<SongSummary> songs = jdbcTemplate.query("""
                        select song_id, title, artist_name, genre_name, mood_tag, duration_text,
                               play_count, like_count, highlight_color, description, audio_url
                        from songs
                        where release_status = 'PUBLISHED'
                          and (? = '' or (title like concat('%', ?, '%')
                               or artist_name like concat('%', ?, '%')
                               or genre_name like concat('%', ?, '%')
                               or mood_tag like concat('%', ?, '%')
                               or description like concat('%', ?, '%')))
                          and (? = '' or lower(?) = 'all' or genre_name = ?)
                        order by like_count desc, play_count desc, created_at desc
                        """,
                (rs, rowNum) -> {
                    PresentationContentOverrides.SongText text = PresentationContentOverrides.song(
                            rs.getString("song_id"),
                            rs.getString("mood_tag"),
                            rs.getString("description")
                    );
                    AudioAccessInfo accessInfo = audioAccessResolver.resolve(rs.getString("audio_url"));

                    return new SongSummary(
                            rs.getString("song_id"),
                            rs.getString("title"),
                            rs.getString("artist_name"),
                            rs.getString("genre_name"),
                            text.moodTag(),
                            rs.getString("duration_text"),
                            rs.getInt("play_count"),
                            rs.getInt("like_count"),
                            rs.getString("highlight_color"),
                            text.description(),
                            accessInfo.streamUrl(),
                            accessInfo.downloadUrl(),
                            accessInfo.available(),
                            accessInfo.sourceType()
                    );
                },
                normalizedKeyword, normalizedKeyword, normalizedKeyword, normalizedKeyword, normalizedKeyword, normalizedKeyword,
                normalizedGenre, normalizedGenre, normalizedGenre
        );

        return new SongListResponse(genres, songs);
    }

    @Override
    public SongDetailResponse getSongDetail(String songId) {
        try {
            SongDetailResponse detail = jdbcTemplate.queryForObject("""
                            select s.song_id, s.title, s.artist_name, s.genre_name, s.mood_tag, s.duration_text,
                                   s.play_count, s.like_count, s.highlight_color, s.description, s.audio_url, s.cover_url,
                                   u.display_name as uploader_name, u.bio as uploader_bio, u.user_id as uploader_id,
                                   (select count(*) from favorites f where f.target_type = 'SONG' and f.target_id = s.song_id) as favorite_count,
                                   (select count(*) from comments c where c.song_id = s.song_id) as comment_count
                            from songs s
                            left join users u on u.user_id = s.uploaded_by_user_id
                            where s.song_id = ? and s.release_status = 'PUBLISHED'
                            """,
                    (rs, rowNum) -> {
                        PresentationContentOverrides.SongText text = PresentationContentOverrides.song(
                                rs.getString("song_id"),
                                rs.getString("mood_tag"),
                                rs.getString("description")
                        );
                        AudioAccessInfo accessInfo = audioAccessResolver.resolve(rs.getString("audio_url"));

                        return new SongDetailResponse(
                                rs.getString("song_id"),
                                rs.getString("title"),
                                rs.getString("artist_name"),
                                rs.getString("genre_name"),
                                text.moodTag(),
                                rs.getString("duration_text"),
                                rs.getInt("play_count"),
                                rs.getInt("like_count"),
                                rs.getInt("favorite_count"),
                                rs.getInt("comment_count"),
                                rs.getString("highlight_color"),
                                text.description(),
                                accessInfo.originalUrl(),
                                accessInfo.streamUrl(),
                                accessInfo.downloadUrl(),
                                accessInfo.available(),
                                accessInfo.sourceType(),
                                rs.getString("cover_url"),
                                rs.getString("uploader_name"),
                                PresentationContentOverrides.userBio(rs.getString("uploader_id"), rs.getString("uploader_bio")),
                                loadSongTags(songId)
                        );
                    },
                    songId
            );

            if (detail == null) {
                throw new IllegalArgumentException("目标歌曲不存在。");
            }
            return detail;
        } catch (EmptyResultDataAccessException exception) {
            throw new IllegalArgumentException("目标歌曲不存在。");
        }
    }

    @Override
    public List<CommentSummary> getSongComments(String songId) {
        return jdbcTemplate.query("""
                        select c.comment_id, u.display_name, c.content, c.like_count, c.created_at, s.title
                        from comments c
                        join users u on u.user_id = c.user_id
                        join songs s on s.song_id = c.song_id
                        where c.song_id = ?
                        order by c.created_at desc
                        """,
                (rs, rowNum) -> new CommentSummary(
                        rs.getString("comment_id"),
                        rs.getString("display_name"),
                        "SONG",
                        rs.getString("title"),
                        PresentationContentOverrides.commentContent(rs.getString("comment_id"), rs.getString("content")),
                        rs.getInt("like_count"),
                        RelativeTimeFormatter.format(rs.getObject("created_at", LocalDateTime.class))
                ),
                songId
        );
    }

    @Override
    public CreateSongResponse createSong(CreateSongRequest request) {
        validateCreateSongRequest(request);

        String songId = "song-" + UUID.randomUUID().toString().replace("-", "").substring(0, 8);
        String title = request.title().trim();
        String artist = request.artist().trim();
        String genre = request.genre().trim();
        String moodTag = request.moodTag() == null ? "" : request.moodTag().trim();
        String durationText = request.durationText().trim();
        String description = request.description() == null ? "" : request.description().trim();
        String audioUrl = request.audioUrl().trim();
        String coverUrl = request.coverUrl() == null ? null : request.coverUrl().trim();

        jdbcTemplate.update("""
                        insert into songs(
                            song_id, title, artist_name, genre_name, mood_tag, duration_seconds, duration_text,
                            play_count, like_count, highlight_color, description, audio_url, cover_url,
                            release_status, uploaded_by_user_id, created_at
                        ) values (?, ?, ?, ?, ?, ?, ?, 0, 0, ?, ?, ?, ?, 'PUBLISHED', ?, now())
                        """,
                songId,
                title,
                artist,
                genre,
                moodTag,
                parseDurationSeconds(durationText),
                durationText,
                PresentationContentOverrides.genreColor(genre, "#3858ff"),
                description,
                audioUrl,
                blankToNull(coverUrl),
                request.userId().trim()
        );

        if (request.tags() != null) {
            for (String tag : request.tags()) {
                if (tag != null && !tag.isBlank()) {
                    jdbcTemplate.update(
                            "insert into song_tags(song_id, tag_name) values (?, ?)",
                            songId,
                            tag.trim()
                    );
                }
            }
        }

        AudioAccessInfo accessInfo = audioAccessResolver.resolve(audioUrl);
        return new CreateSongResponse(
                songId,
                title,
                audioUrl,
                accessInfo.streamUrl(),
                accessInfo.downloadUrl(),
                accessInfo.sourceType()
        );
    }

    private List<String> loadSongTags(String songId) {
        return jdbcTemplate.query(
                "select tag_name from song_tags where song_id = ? order by tag_name",
                (rs, rowNum) -> rs.getString("tag_name"),
                songId
        );
    }

    private void validateCreateSongRequest(CreateSongRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("歌曲创建请求不能为空。");
        }
        if (isBlank(request.userId()) || isBlank(request.title()) || isBlank(request.artist())
                || isBlank(request.genre()) || isBlank(request.durationText()) || isBlank(request.audioUrl())) {
            throw new IllegalArgumentException("歌曲标题、歌手、曲风、时长和音频地址不能为空。");
        }

        Integer creatorCount = jdbcTemplate.queryForObject(
                "select count(*) from users where user_id = ? and role_code in ('CREATOR', 'ADMIN')",
                Integer.class,
                request.userId().trim()
        );
        if (creatorCount == null || creatorCount == 0) {
            throw new IllegalArgumentException("当前用户没有上传歌曲权限。");
        }

        String audioUrl = request.audioUrl().trim();
        if (!(audioUrl.startsWith("http://") || audioUrl.startsWith("https://") || audioUrl.startsWith("/media/audio/"))) {
            throw new IllegalArgumentException("音频地址必须是 http/https 外部地址或 /media/audio/ 本地资源地址。");
        }

        parseDurationSeconds(request.durationText().trim());
    }

    private int parseDurationSeconds(String durationText) {
        String[] parts = durationText.split(":");
        if (parts.length != 2) {
            throw new IllegalArgumentException("时长格式必须是 mm:ss。");
        }

        try {
            int minutes = Integer.parseInt(parts[0]);
            int seconds = Integer.parseInt(parts[1]);
            if (minutes < 0 || seconds < 0 || seconds >= 60) {
                throw new NumberFormatException("invalid");
            }
            return minutes * 60 + seconds;
        } catch (NumberFormatException exception) {
            throw new IllegalArgumentException("时长格式必须是 mm:ss。");
        }
    }

    private String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value;
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
