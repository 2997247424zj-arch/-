package com.example.musicshareplatformback.comment.service.impl;

import com.example.musicshareplatformback.comment.dto.CreateCommentRequest;
import com.example.musicshareplatformback.comment.service.CommentService;
import com.example.musicshareplatformback.community.dto.CommentSummary;
import com.example.musicshareplatformback.support.PresentationContentOverrides;
import com.example.musicshareplatformback.support.RelativeTimeFormatter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class JdbcCommentService implements CommentService {

    private final JdbcTemplate jdbcTemplate;

    public JdbcCommentService(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public CommentSummary createComment(CreateCommentRequest request) {
        validate(request);

        String commentId = "comment-" + UUID.randomUUID().toString().replace("-", "").substring(0, 8);
        jdbcTemplate.update("""
                        insert into comments(comment_id, user_id, song_id, playlist_id, parent_comment_id, content, like_count, created_at)
                        values (?, ?, ?, ?, ?, ?, 0, now())
                        """,
                commentId,
                request.userId(),
                blankToNull(request.songId()),
                blankToNull(request.playlistId()),
                blankToNull(request.parentCommentId()),
                request.content().trim()
        );

        return jdbcTemplate.queryForObject("""
                        select c.comment_id, c.playlist_id, u.display_name, c.content, c.like_count, c.created_at,
                               case when c.song_id is not null then 'SONG' else 'PLAYLIST' end as target_type,
                               coalesce(s.title, p.title) as target_name
                        from comments c
                        join users u on u.user_id = c.user_id
                        left join songs s on s.song_id = c.song_id
                        left join playlists p on p.playlist_id = c.playlist_id
                        where c.comment_id = ?
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
                            rs.getString("content"),
                            rs.getInt("like_count"),
                            RelativeTimeFormatter.format(rs.getObject("created_at", LocalDateTime.class))
                    );
                },
                commentId
        );
    }

    private void validate(CreateCommentRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("评论请求不能为空。");
        }
        if (isBlank(request.userId())) {
            throw new IllegalArgumentException("评论用户不能为空。");
        }
        if (isBlank(request.content())) {
            throw new IllegalArgumentException("评论内容不能为空。");
        }
        boolean hasSong = !isBlank(request.songId());
        boolean hasPlaylist = !isBlank(request.playlistId());
        if (hasSong == hasPlaylist) {
            throw new IllegalArgumentException("评论必须且只能关联一首歌曲或一个歌单。");
        }

        Integer userCount = jdbcTemplate.queryForObject("select count(*) from users where user_id = ?", Integer.class, request.userId());
        if (userCount == null || userCount == 0) {
            throw new IllegalArgumentException("评论用户不存在。");
        }

        if (hasSong) {
            Integer songCount = jdbcTemplate.queryForObject("select count(*) from songs where song_id = ?", Integer.class, request.songId());
            if (songCount == null || songCount == 0) {
                throw new IllegalArgumentException("目标歌曲不存在。");
            }
        }

        if (hasPlaylist) {
            Integer playlistCount = jdbcTemplate.queryForObject("select count(*) from playlists where playlist_id = ?", Integer.class, request.playlistId());
            if (playlistCount == null || playlistCount == 0) {
                throw new IllegalArgumentException("目标歌单不存在。");
            }
        }

        if (!isBlank(request.parentCommentId())) {
            Integer parentCount = jdbcTemplate.queryForObject("select count(*) from comments where comment_id = ?", Integer.class, request.parentCommentId());
            if (parentCount == null || parentCount == 0) {
                throw new IllegalArgumentException("父评论不存在。");
            }
        }
    }

    private String blankToNull(String value) {
        return isBlank(value) ? null : value.trim();
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
