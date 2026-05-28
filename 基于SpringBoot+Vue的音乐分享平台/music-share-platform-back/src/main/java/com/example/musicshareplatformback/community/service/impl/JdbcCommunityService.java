package com.example.musicshareplatformback.community.service.impl;

import com.example.musicshareplatformback.community.dto.CommentSummary;
import com.example.musicshareplatformback.community.dto.CommunityResponse;
import com.example.musicshareplatformback.community.service.CommunityService;
import com.example.musicshareplatformback.home.dto.ActivitySummary;
import com.example.musicshareplatformback.support.PresentationContentOverrides;
import com.example.musicshareplatformback.support.RelativeTimeFormatter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class JdbcCommunityService implements CommunityService {

    private final JdbcTemplate jdbcTemplate;

    public JdbcCommunityService(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public CommunityResponse getCommunitySnapshot() {
        Integer totalComments = jdbcTemplate.queryForObject("select count(*) from comments", Integer.class);
        Integer totalPlaylists = jdbcTemplate.queryForObject("select count(*) from playlists where is_public = 1", Integer.class);
        Integer totalCreators = jdbcTemplate.queryForObject("select count(*) from users where role_code = 'CREATOR'", Integer.class);

        return new CommunityResponse(
                totalComments == null ? 0 : totalComments,
                totalPlaylists == null ? 0 : totalPlaylists,
                totalCreators == null ? 0 : totalCreators,
                loadLatestActivities(),
                loadLatestComments()
        );
    }

    private List<ActivitySummary> loadLatestActivities() {
        return jdbcTemplate.query("""
                        select activity_id, user_name, action_type, target_name, created_at
                        from activity_logs
                        order by created_at desc
                        limit 6
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

    private List<CommentSummary> loadLatestComments() {
        return jdbcTemplate.query("""
                        select c.comment_id, c.playlist_id, u.display_name, c.content, c.like_count, c.created_at,
                               case
                                   when c.song_id is not null then 'SONG'
                                   when c.playlist_id is not null then 'PLAYLIST'
                                   else 'UNKNOWN'
                               end as target_type,
                               coalesce(s.title, p.title) as target_name
                        from comments c
                        join users u on u.user_id = c.user_id
                        left join songs s on s.song_id = c.song_id
                        left join playlists p on p.playlist_id = c.playlist_id
                        order by c.created_at desc
                        limit 6
                        """,
                (rs, rowNum) -> {
                    String targetName = rs.getString("target_name");
                    if ("PLAYLIST".equals(rs.getString("target_type"))) {
                        targetName = PresentationContentOverrides.playlist(
                                rs.getString("playlist_id"),
                                targetName,
                                "",
                                ""
                        ).title();
                    }

                    return new CommentSummary(
                            rs.getString("comment_id"),
                            rs.getString("display_name"),
                            rs.getString("target_type"),
                            targetName,
                            PresentationContentOverrides.commentContent(rs.getString("comment_id"), rs.getString("content")),
                            rs.getInt("like_count"),
                            RelativeTimeFormatter.format(rs.getObject("created_at", LocalDateTime.class))
                    );
                }
        );
    }
}
