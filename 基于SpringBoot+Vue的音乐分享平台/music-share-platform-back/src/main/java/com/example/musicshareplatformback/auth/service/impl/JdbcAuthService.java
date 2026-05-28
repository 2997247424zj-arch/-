package com.example.musicshareplatformback.auth.service.impl;

import com.example.musicshareplatformback.auth.dto.LoginRequest;
import com.example.musicshareplatformback.auth.dto.LoginResponse;
import com.example.musicshareplatformback.auth.dto.RegisterRequest;
import com.example.musicshareplatformback.auth.dto.UserProfile;
import com.example.musicshareplatformback.auth.service.AuthService;
import com.example.musicshareplatformback.support.PresentationContentOverrides;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class JdbcAuthService implements AuthService {

    private static final List<String> AVATAR_COLORS = List.of(
            "#3858ff",
            "#ff8a5b",
            "#6fcf97",
            "#56ccf2",
            "#bb6bd9",
            "#f4b942"
    );

    private final JdbcTemplate jdbcTemplate;

    public JdbcAuthService(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public LoginResponse login(LoginRequest request) {
        if (request == null || isBlank(request.username()) || isBlank(request.password())) {
            throw new IllegalArgumentException("用户名和密码不能为空。");
        }

        try {
            UserProfile profile = jdbcTemplate.queryForObject("""
                            select user_id, username, display_name, role_code, favorite_genre, bio
                            from users
                            where username = ? and password_plain = ?
                            """,
                    (rs, rowNum) -> new UserProfile(
                            rs.getString("user_id"),
                            rs.getString("username"),
                            rs.getString("display_name"),
                            rs.getString("role_code"),
                            rs.getString("favorite_genre"),
                            PresentationContentOverrides.userBio(rs.getString("user_id"), rs.getString("bio"))
                    ),
                    request.username(),
                    request.password()
            );

            if (profile == null) {
                throw new IllegalArgumentException("用户名或密码错误。");
            }

            return new LoginResponse(
                    "token-" + profile.id(),
                    profile,
                    buildCapabilities(profile.role())
            );
        } catch (EmptyResultDataAccessException exception) {
            throw new IllegalArgumentException("用户名或密码错误。可用演示账号：demo_admin / demo_creator / demo_user，密码均为 123456。");
        }
    }

    @Override
    public LoginResponse register(RegisterRequest request) {
        validateRegisterRequest(request);

        String username = request.username().trim();
        String password = request.password();
        String displayName = request.displayName().trim();
        String role = normalizeRole(request.role());
        String favoriteGenre = normalizeFavoriteGenre(request.favoriteGenre());
        String bio = normalizeBio(request.bio(), displayName);
        String userId = "user-" + UUID.randomUUID().toString().replace("-", "").substring(0, 12);

        try {
            jdbcTemplate.update("""
                            insert into users(
                                user_id, username, password_plain, display_name, role_code,
                                favorite_genre, bio, avatar_color, created_at
                            ) values (?, ?, ?, ?, ?, ?, ?, ?, now())
                            """,
                    userId,
                    username,
                    password,
                    displayName,
                    role,
                    favoriteGenre,
                    bio,
                    avatarColor(username)
            );
        } catch (DuplicateKeyException exception) {
            throw new IllegalArgumentException("该用户名已被使用。");
        }

        UserProfile profile = new UserProfile(userId, username, displayName, role, favoriteGenre, bio);
        return new LoginResponse(
                "token-" + profile.id(),
                profile,
                buildCapabilities(profile.role())
        );
    }

    private void validateRegisterRequest(RegisterRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("注册请求不能为空。");
        }
        if (isBlank(request.username()) || isBlank(request.password()) || isBlank(request.displayName())) {
            throw new IllegalArgumentException("用户名、密码和昵称不能为空。");
        }
        if (request.username().trim().length() < 3 || request.username().trim().length() > 50) {
            throw new IllegalArgumentException("用户名长度需要在 3 到 50 个字符之间。");
        }
        if (request.password().length() < 6 || request.password().length() > 100) {
            throw new IllegalArgumentException("密码长度需要在 6 到 100 个字符之间。");
        }
        if (request.displayName().trim().length() > 100) {
            throw new IllegalArgumentException("昵称不能超过 100 个字符。");
        }
        if (!isBlank(request.bio()) && request.bio().trim().length() > 255) {
            throw new IllegalArgumentException("简介不能超过 255 个字符。");
        }
    }

    private String normalizeRole(String role) {
        return "CREATOR".equals(role) ? "CREATOR" : "USER";
    }

    private String normalizeFavoriteGenre(String favoriteGenre) {
        return isBlank(favoriteGenre) ? "Lo-fi" : favoriteGenre.trim();
    }

    private String normalizeBio(String bio, String displayName) {
        return isBlank(bio) ? displayName + " 正在建立自己的音乐收藏。" : bio.trim();
    }

    private String avatarColor(String username) {
        return AVATAR_COLORS.get(Math.floorMod(username.hashCode(), AVATAR_COLORS.size()));
    }

    private List<String> buildCapabilities(String role) {
        List<String> capabilities = new ArrayList<>(List.of(
                "home:view",
                "songs:view",
                "playlists:view",
                "community:view"
        ));

        if ("CREATOR".equals(role) || "ADMIN".equals(role)) {
            capabilities.add("songs:upload");
            capabilities.add("playlists:edit");
        }
        if ("ADMIN".equals(role)) {
            capabilities.add("community:moderate");
            capabilities.add("admin:manage");
        }

        return capabilities;
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
