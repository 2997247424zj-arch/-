package com.example.musicshareplatformback.favorite.service.impl;

import com.example.musicshareplatformback.favorite.dto.FavoriteRequest;
import com.example.musicshareplatformback.favorite.dto.FavoriteResponse;
import com.example.musicshareplatformback.favorite.service.FavoriteService;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.Locale;
import java.util.UUID;

@Service
public class JdbcFavoriteService implements FavoriteService {

    private final JdbcTemplate jdbcTemplate;

    public JdbcFavoriteService(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public FavoriteResponse createFavorite(FavoriteRequest request) {
        validate(request);
        String targetType = request.targetType().trim().toUpperCase(Locale.ROOT);
        String targetId = request.targetId().trim();

        try {
            jdbcTemplate.update("""
                            insert into favorites(favorite_id, user_id, target_type, target_id, created_at)
                            values (?, ?, ?, ?, now())
                            """,
                    "favorite-" + UUID.randomUUID().toString().replace("-", "").substring(0, 8),
                    request.userId().trim(),
                    targetType,
                    targetId
            );
        } catch (DuplicateKeyException exception) {
            throw new IllegalArgumentException("你已经收藏过这个内容了。");
        }

        Integer totalFavorites = jdbcTemplate.queryForObject(
                "select count(*) from favorites where target_type = ? and target_id = ?",
                Integer.class,
                targetType,
                targetId
        );

        return new FavoriteResponse(
                targetType,
                targetId,
                totalFavorites == null ? 0 : totalFavorites
        );
    }

    private void validate(FavoriteRequest request) {
        if (request == null || isBlank(request.userId()) || isBlank(request.targetType()) || isBlank(request.targetId())) {
            throw new IllegalArgumentException("收藏请求参数不完整。");
        }

        String targetType = request.targetType().trim().toUpperCase(Locale.ROOT);
        String targetId = request.targetId().trim();
        if (!"SONG".equals(targetType) && !"PLAYLIST".equals(targetType)) {
            throw new IllegalArgumentException("收藏目标类型不支持。");
        }

        Integer userCount = jdbcTemplate.queryForObject("select count(*) from users where user_id = ?", Integer.class, request.userId().trim());
        if (userCount == null || userCount == 0) {
            throw new IllegalArgumentException("收藏用户不存在。");
        }

        String tableName = "SONG".equals(targetType) ? "songs" : "playlists";
        String columnName = "SONG".equals(targetType) ? "song_id" : "playlist_id";
        Integer targetCount = jdbcTemplate.queryForObject(
                "select count(*) from " + tableName + " where " + columnName + " = ?",
                Integer.class,
                targetId
        );
        if (targetCount == null || targetCount == 0) {
            throw new IllegalArgumentException("收藏目标不存在。");
        }
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
