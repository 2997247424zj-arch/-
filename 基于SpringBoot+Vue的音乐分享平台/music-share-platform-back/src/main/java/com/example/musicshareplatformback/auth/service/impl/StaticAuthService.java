package com.example.musicshareplatformback.auth.service.impl;

import com.example.musicshareplatformback.auth.dto.LoginRequest;
import com.example.musicshareplatformback.auth.dto.LoginResponse;
import com.example.musicshareplatformback.auth.dto.RegisterRequest;
import com.example.musicshareplatformback.auth.dto.UserProfile;
import com.example.musicshareplatformback.auth.service.AuthService;

import java.util.List;
import java.util.Map;

public class StaticAuthService implements AuthService {

    private static final Map<String, DemoUser> DEMO_USERS = Map.of(
            "demo_admin", new DemoUser(
                    "admin-1",
                    "demo_admin",
                    "Music Ops",
                    "ADMIN",
                    "Electronic",
                    "负责审核推荐内容与维护首页运营位。",
                    "123456"
            ),
            "demo_creator", new DemoUser(
                    "creator-1",
                    "demo_creator",
                    "Aurora Lane",
                    "CREATOR",
                    "City Pop",
                    "上传独立流行和夜行感编曲 demo。",
                    "123456"
            ),
            "demo_user", new DemoUser(
                    "user-1",
                    "demo_user",
                    "Night Listener",
                    "USER",
                    "Lo-fi",
                    "习惯收藏学习向歌单和深夜电台内容。",
                    "123456"
            )
    );

    @Override
    public LoginResponse login(LoginRequest request) {
        if (request == null || isBlank(request.username()) || isBlank(request.password())) {
            throw new IllegalArgumentException("用户名和密码不能为空。");
        }

        DemoUser demoUser = DEMO_USERS.get(request.username());
        if (demoUser == null || !demoUser.password().equals(request.password())) {
            throw new IllegalArgumentException("用户名或密码错误。可用演示账号：demo_admin / demo_creator / demo_user，密码均为 123456。");
        }

        return new LoginResponse(
                "token-" + demoUser.id(),
                new UserProfile(
                        demoUser.id(),
                        demoUser.username(),
                        demoUser.displayName(),
                        demoUser.role(),
                        demoUser.favoriteGenre(),
                        demoUser.bio()
                ),
                List.of("home:view", "songs:view", "playlists:view", "community:view")
        );
    }

    @Override
    public LoginResponse register(RegisterRequest request) {
        validateRegisterRequest(request);

        if (DEMO_USERS.containsKey(request.username().trim())) {
            throw new IllegalArgumentException("该用户名已被使用。");
        }

        String role = normalizeRole(request.role());
        String userId = "user-static-" + Integer.toHexString(request.username().trim().hashCode()).replace("-", "");
        return new LoginResponse(
                "token-" + userId,
                new UserProfile(
                        userId,
                        request.username().trim(),
                        request.displayName().trim(),
                        role,
                        normalizeFavoriteGenre(request.favoriteGenre()),
                        normalizeBio(request.bio(), request.displayName())
                ),
                buildCapabilities(role)
        );
    }

    private List<String> buildCapabilities(String role) {
        if ("CREATOR".equals(role)) {
            return List.of("home:view", "songs:view", "playlists:view", "community:view", "songs:upload", "playlists:edit");
        }
        return List.of("home:view", "songs:view", "playlists:view", "community:view");
    }

    private void validateRegisterRequest(RegisterRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("注册请求不能为空。");
        }
        if (isBlank(request.username()) || isBlank(request.password()) || isBlank(request.displayName())) {
            throw new IllegalArgumentException("用户名、密码和昵称不能为空。");
        }
        if (request.username().trim().length() < 3) {
            throw new IllegalArgumentException("用户名至少需要 3 个字符。");
        }
        if (request.password().length() < 6) {
            throw new IllegalArgumentException("密码至少需要 6 个字符。");
        }
    }

    private String normalizeRole(String role) {
        return "CREATOR".equals(role) ? "CREATOR" : "USER";
    }

    private String normalizeFavoriteGenre(String favoriteGenre) {
        return isBlank(favoriteGenre) ? "Lo-fi" : favoriteGenre.trim();
    }

    private String normalizeBio(String bio, String displayName) {
        return isBlank(bio) ? displayName.trim() + " 正在建立自己的音乐收藏。" : bio.trim();
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    private record DemoUser(
            String id,
            String username,
            String displayName,
            String role,
            String favoriteGenre,
            String bio,
            String password
    ) {
    }
}
