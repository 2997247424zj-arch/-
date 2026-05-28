package com.example.musicshareplatformback.like.service.impl;

import com.example.musicshareplatformback.like.dto.LikeRequest;
import com.example.musicshareplatformback.like.dto.LikeResponse;
import com.example.musicshareplatformback.like.service.LikeService;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.Locale;
import java.util.UUID;

@Service
public class JdbcLikeService implements LikeService {

    private final JdbcTemplate jdbcTemplate;

    public JdbcLikeService(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public LikeResponse createLike(LikeRequest request) {
        validate(request);

        String targetType = request.targetType().trim().toUpperCase(Locale.ROOT);
        String targetId = request.targetId().trim();

        try {
            jdbcTemplate.update("""
                            insert into interaction_likes(like_id, user_id, target_type, target_id, created_at)
                            values (?, ?, ?, ?, now())
                            """,
                    "like-" + UUID.randomUUID().toString().replace("-", "").substring(0, 8),
                    request.userId().trim(),
                    targetType,
                    targetId
            );
        } catch (DuplicateKeyException exception) {
            throw new IllegalArgumentException("你已经点赞过这个内容了。");
        }

        Integer totalLikes = jdbcTemplate.queryForObject(
                "select count(*) from interaction_likes where target_type = ? and target_id = ?",
                Integer.class,
                targetType,
                targetId
        );

        if ("SONG".equals(targetType)) {
            jdbcTemplate.update("""
                            update songs
                            set like_count = (select count(*) from interaction_likes where target_type = 'SONG' and target_id = ?)
                            where song_id = ?
                            """,
                    targetId,
                    targetId
            );
        } else if ("COMMENT".equals(targetType)) {
            jdbcTemplate.update("""
                            update comments
                            set like_count = (select count(*) from interaction_likes where target_type = 'COMMENT' and target_id = ?)
                            where comment_id = ?
                            """,
                    targetId,
                    targetId
            );
        }

        return new LikeResponse(targetType, targetId, totalLikes == null ? 0 : totalLikes);
    }

    private void validate(LikeRequest request) {
        if (request == null || isBlank(request.userId()) || isBlank(request.targetType()) || isBlank(request.targetId())) {
            throw new IllegalArgumentException("点赞请求参数不完整。");
        }

        String targetType = request.targetType().trim().toUpperCase(Locale.ROOT);
        String targetId = request.targetId().trim();
        if (!"SONG".equals(targetType) && !"COMMENT".equals(targetType) && !"PLAYLIST".equals(targetType)) {
            throw new IllegalArgumentException("点赞目标类型不支持。");
        }

        Integer userCount = jdbcTemplate.queryForObject("select count(*) from users where user_id = ?", Integer.class, request.userId().trim());
        if (userCount == null || userCount == 0) {
            throw new IllegalArgumentException("点赞用户不存在。");
        }

        String tableName = switch (targetType) {
            case "SONG" -> "songs";
            case "COMMENT" -> "comments";
            default -> "playlists";
        };
        String columnName = switch (targetType) {
            case "SONG" -> "song_id";
            case "COMMENT" -> "comment_id";
            default -> "playlist_id";
        };

        Integer targetCount = jdbcTemplate.queryForObject(
                "select count(*) from " + tableName + " where " + columnName + " = ?",
                Integer.class,
                targetId
        );
        if (targetCount == null || targetCount == 0) {
            throw new IllegalArgumentException("点赞目标不存在。");
        }
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
