package com.example.musicshareplatformback.home.service.impl;

import com.example.musicshareplatformback.home.dto.ActivitySummary;
import com.example.musicshareplatformback.home.dto.GenreSummary;
import com.example.musicshareplatformback.home.dto.HomePageResponse;
import com.example.musicshareplatformback.home.dto.HomePlayableTrack;
import com.example.musicshareplatformback.home.dto.PlatformOverview;
import com.example.musicshareplatformback.home.dto.PlaylistSummary;
import com.example.musicshareplatformback.home.dto.TrackSummary;
import com.example.musicshareplatformback.home.service.HomeService;
import com.example.musicshareplatformback.media.AudioAccessInfo;
import com.example.musicshareplatformback.media.AudioAccessResolver;
import com.example.musicshareplatformback.support.PresentationContentOverrides;
import com.example.musicshareplatformback.support.RelativeTimeFormatter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class JdbcHomeService implements HomeService {

    private final JdbcTemplate jdbcTemplate;
    private final AudioAccessResolver audioAccessResolver;

    public JdbcHomeService(JdbcTemplate jdbcTemplate, AudioAccessResolver audioAccessResolver) {
        this.jdbcTemplate = jdbcTemplate;
        this.audioAccessResolver = audioAccessResolver;
    }

    @Override
    public HomePageResponse getHomePage() {
        return new HomePageResponse(
                loadOverview(),
                loadGenres(),
                loadTrendingTracks(),
                loadRecommendedSleepTracks(),
                loadFeaturedPlaylists(),
                loadLatestActivities(),
                List.of(
                        "第 1 周：完成用户、歌曲、歌单、评论四个核心实体建模。",
                        "第 2 周：接入数据库与鉴权，补充上传、点赞、收藏能力。",
                        "第 3 周：完成发现页、个人音乐库、社区讨论与后台管理。"
                )
        );
    }

    private PlatformOverview loadOverview() {
        Integer totalTracks = jdbcTemplate.queryForObject(
                "select count(*) from songs where release_status = 'PUBLISHED'",
                Integer.class
        );
        Integer totalPlaylists = jdbcTemplate.queryForObject(
                "select count(*) from playlists where is_public = 1",
                Integer.class
        );
        Integer activeCreators = jdbcTemplate.queryForObject(
                "select count(*) from users where role_code = 'CREATOR'",
                Integer.class
        );
        Integer dailyShares = jdbcTemplate.queryForObject(
                "select count(*) from activity_logs where created_at >= current_date()",
                Integer.class
        );

        return new PlatformOverview(
                valueOrZero(totalTracks),
                valueOrZero(totalPlaylists),
                valueOrZero(activeCreators),
                valueOrZero(dailyShares)
        );
    }

    private List<GenreSummary> loadGenres() {
        return jdbcTemplate.query("""
                        select concat('genre-', lower(replace(genre_name, ' ', '-'))) as genre_id,
                               genre_name,
                               count(*) as track_count,
                               min(highlight_color) as accent_color
                        from songs
                        where release_status = 'PUBLISHED'
                        group by genre_name
                        order by track_count desc, genre_name
                        limit 4
                        """,
                (rs, rowNum) -> new GenreSummary(
                        rs.getString("genre_id"),
                        rs.getString("genre_name"),
                        PresentationContentOverrides.genreDescription(rs.getString("genre_name")),
                        PresentationContentOverrides.genreColor(rs.getString("genre_name"), rs.getString("accent_color")),
                        rs.getInt("track_count")
                )
        );
    }

    private List<TrackSummary> loadTrendingTracks() {
        return jdbcTemplate.query("""
                        select song_id, title, artist_name, genre_name, mood_tag, duration_text,
                               like_count, description, highlight_color, audio_url
                        from songs
                        where release_status = 'PUBLISHED'
                        order by like_count desc, play_count desc, created_at desc
                        limit 4
                        """,
                (rs, rowNum) -> {
                    PresentationContentOverrides.SongText text = PresentationContentOverrides.song(
                            rs.getString("song_id"),
                            rs.getString("mood_tag"),
                            rs.getString("description")
                    );
                    AudioAccessInfo accessInfo = audioAccessResolver.resolve(rs.getString("audio_url"));

                    return new TrackSummary(
                            rs.getString("song_id"),
                            rs.getString("title"),
                            rs.getString("artist_name"),
                            rs.getString("genre_name"),
                            text.moodTag(),
                            rs.getString("duration_text"),
                            text.description(),
                            rs.getString("highlight_color"),
                            rs.getInt("like_count"),
                            accessInfo.streamUrl(),
                            accessInfo.downloadUrl(),
                            accessInfo.available(),
                            accessInfo.sourceType()
                    );
                }
        );
    }

    private List<HomePlayableTrack> loadRecommendedSleepTracks() {
        return jdbcTemplate.query("""
                        select s.song_id, s.title, s.artist_name, s.genre_name, s.mood_tag, s.duration_text,
                               s.description, s.highlight_color, s.audio_url
                        from songs s
                        where s.release_status = 'PUBLISHED'
                          and (
                              s.mood_tag in ('失眠安抚', '夜色轻拍', '凌晨发呆', '耳机助眠', '雨夜缓行', '深夜降噪')
                              or exists (
                                  select 1
                                  from song_tags st
                                  where st.song_id = s.song_id
                                    and st.tag_name in ('助眠', '失眠', '深夜', '放松', '安静', '低音量', '耳机', '雨夜')
                              )
                          )
                        order by s.like_count desc, s.play_count desc, s.created_at desc
                        limit 6
                        """,
                (rs, rowNum) -> {
                    AudioAccessInfo accessInfo = audioAccessResolver.resolve(rs.getString("audio_url"));
                    return new HomePlayableTrack(
                            rs.getString("song_id"),
                            rs.getString("title"),
                            rs.getString("artist_name"),
                            rs.getString("genre_name"),
                            rs.getString("mood_tag"),
                            rs.getString("duration_text"),
                            rs.getString("description"),
                            rs.getString("highlight_color"),
                            accessInfo.streamUrl(),
                            accessInfo.downloadUrl(),
                            accessInfo.available(),
                            accessInfo.sourceType()
                    );
                }
        );
    }

    private List<PlaylistSummary> loadFeaturedPlaylists() {
        return jdbcTemplate.query("""
                        select playlist_id, title, curator_name, description, track_count, follower_count, scene_tag
                        from playlists
                        where is_public = 1
                        order by follower_count desc, created_at desc
                        limit 3
                        """,
                (rs, rowNum) -> {
                    PresentationContentOverrides.PlaylistText text = PresentationContentOverrides.playlist(
                            rs.getString("playlist_id"),
                            rs.getString("title"),
                            rs.getString("scene_tag"),
                            rs.getString("description")
                    );

                    return new PlaylistSummary(
                            rs.getString("playlist_id"),
                            text.title(),
                            rs.getString("curator_name"),
                            text.description(),
                            rs.getInt("track_count"),
                            rs.getInt("follower_count")
                    );
                }
        );
    }

    private List<ActivitySummary> loadLatestActivities() {
        return jdbcTemplate.query("""
                        select activity_id, user_name, action_type, target_name, created_at
                        from activity_logs
                        order by created_at desc
                        limit 4
                        """,
                (rs, rowNum) -> new ActivitySummary(
                        rs.getString("activity_id"),
                        rs.getString("user_name"),
                        PresentationContentOverrides.activityAction(rs.getString("activity_id"), rs.getString("action_type")),
                        PresentationContentOverrides.activityTarget(rs.getString("activity_id"), rs.getString("target_name")),
                        RelativeTimeFormatter.format(rs.getObject("created_at", LocalDateTime.class))
                )
        );
    }

    private int valueOrZero(Integer value) {
        return value == null ? 0 : value;
    }
}
