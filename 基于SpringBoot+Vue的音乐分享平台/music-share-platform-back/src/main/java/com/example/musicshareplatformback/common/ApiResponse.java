package com.example.musicshareplatformback.common;

import java.time.OffsetDateTime;

public record ApiResponse<T>(
        boolean success,
        String message,
        String timestamp,
        T data
) {
    public static <T> ApiResponse<T> success(String message, T data) {
        return new ApiResponse<>(true, message, OffsetDateTime.now().toString(), data);
    }

    public static <T> ApiResponse<T> failure(String message, T data) {
        return new ApiResponse<>(false, message, OffsetDateTime.now().toString(), data);
    }
}
