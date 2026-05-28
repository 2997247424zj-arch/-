package com.example.musicshareplatformback.auth.dto;

import java.util.List;

public record LoginResponse(
        String accessToken,
        UserProfile profile,
        List<String> capabilities
) {
}
