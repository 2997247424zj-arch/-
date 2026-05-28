package com.example.musicshareplatformback.comment.dto;

public record CreateCommentRequest(
        String userId,
        String songId,
        String playlistId,
        String parentCommentId,
        String content
) {
}
