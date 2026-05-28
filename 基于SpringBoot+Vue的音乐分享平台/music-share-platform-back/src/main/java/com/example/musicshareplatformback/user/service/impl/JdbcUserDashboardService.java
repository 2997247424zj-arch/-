package com.example.musicshareplatformback.user.service.impl;

import com.example.musicshareplatformback.support.PresentationContentOverrides;
import com.example.musicshareplatformback.support.RelativeTimeFormatter;
import com.example.musicshareplatformback.user.dto.RecentActionSummary;
import com.example.musicshareplatformback.user.dto.UserDashboardResponse;
import com.example.musicshareplatformback.user.service.UserDashboardService;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class JdbcUserDashboardService implements UserDashboardService {

    private final JdbcTemplate jdbcTemplate;

    public JdbcUserDashboardService(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public UserDashboardResponse getDashboard(String userId) {
        try {
            return jdbcTemplate.queryForObject("""
                            select user_id, display_name, role_code, favorite_genre, bio,
                                   (select count(*) from favorites f where f.user_id = u.user_id) as total_favorites,
                                   (select count(*) from interaction_likes l where l.user_id = u.user_id) as total_likes,
                                   (select count(*) from comments c where c.user_id = u.user_id) as total_comments
                            from users u
                            where u.user_id = ?
                            """,
                    (rs, rowNum) -> new UserDashboardResponse(
                            rs.getString("user_id"),
                            rs.getString("display_name"),
                            rs.getString("role_code"),
                            rs.getString("favorite_genre"),
                            PresentationContentOverrides.userBio(rs.getString("user_id"), rs.getString("bio")),
                            rs.getInt("total_favorites"),
                            rs.getInt("total_likes"),
                            rs.getInt("total_comments"),
                            loadRecentFavorites(userId),
                            loadRecentLikes(userId),
                            loadRecentComments(userId)
                    ),
                    userId
            );
        } catch (EmptyResultDataAccessException exception) {
            throw new IllegalArgumentException("目标用户不存在。");
        }
    }

    private List<RecentActionSummary> loadRecentFavorites(String userId) {
        return jdbcTemplate.query("""
                        select favorite_id, target_type, target_id, created_at,
                               case
                                   when target_type = 'SONG' then (select title from songs where song_id = f.target_id)
                                   else (select title from playlists where playlist_id = f.target_id)
                               end as title
                        from favorites f
                        where user_id = ?
                        order by created_at desc
                        limit 5
                        """,
                (rs, rowNum) -> new RecentActionSummary(
                        rs.getString("favorite_id"),
                        rs.getString("target_type"),
                        overrideTitle(rs.getString("target_type"), rs.getString("target_id"), rs.getString("title")),
                        "最近加入收藏",
                        RelativeTimeFormatter.format(rs.getObject("created_at", LocalDateTime.class))
                ),
                userId
        );
    }

    private List<RecentActionSummary> loadRecentLikes(String userId) {
        return jdbcTemplate.query("""
                        select like_id, target_type, target_id, created_at
                        from interaction_likes
                        where user_id = ?
                        order by created_at desc
                        limit 5
                        """,
                (rs, rowNum) -> new RecentActionSummary(
                        rs.getString("like_id"),
                        rs.getString("target_type"),
                        resolveLikeTitle(rs.getString("target_type"), rs.getString("target_id")),
                        "最近点赞",
                        RelativeTimeFormatter.format(rs.getObject("created_at", LocalDateTime.class))
                ),
                userId
        );
    }

    private List<RecentActionSummary> loadRecentComments(String userId) {
        return jdbcTemplate.query("""
                        select comment_id, song_id, playlist_id, content, created_at
                        from comments
                        where user_id = ?
                        order by created_at desc
                        limit 5
                        """,
                (rs, rowNum) -> new RecentActionSummary(
                        rs.getString("comment_id"),
                        rs.getString("song_id") != null ? "SONG" : "PLAYLIST",
                        rs.getString("song_id") != null
                                ? jdbcTemplate.queryForObject("select title from songs where song_id = ?", String.class, rs.getString("song_id"))
                                : PresentationContentOverrides.playlist(
                                        rs.getString("playlist_id"),
                                        jdbcTemplate.queryForObject("select title from playlists where playlist_id = ?", String.class, rs.getString("playlist_id")),
                                        "",
                                        ""
                                ).title(),
                        PresentationContentOverrides.commentContent(rs.getString("comment_id"), rs.getString("content")),
                        RelativeTimeFormatter.format(rs.getObject("created_at", LocalDateTime.class))
                ),
                userId
        );
    }

    private String resolveLikeTitle(String targetType, String targetId) {
        return switch (targetType) {
            case "SONG" -> jdbcTemplate.queryForObject("select title from songs where song_id = ?", String.class, targetId);
            case "PLAYLIST" -> PresentationContentOverrides.playlist(
                    targetId,
                    jdbcTemplate.queryForObject("select title from playlists where playlist_id = ?", String.class, targetId),
                    "",
                    ""
            ).title();
            default -> PresentationContentOverrides.commentContent(
                    targetId,
                    jdbcTemplate.queryForObject("select content from comments where comment_id = ?", String.class, targetId)
            );
        };
    }

    private String overrideTitle(String targetType, String targetId, String fallbackTitle) {
        if ("PLAYLIST".equals(targetType)) {
            return PresentationContentOverrides.playlist(targetId, fallbackTitle, "", "").title();
        }
        return fallbackTitle;
    }
}
