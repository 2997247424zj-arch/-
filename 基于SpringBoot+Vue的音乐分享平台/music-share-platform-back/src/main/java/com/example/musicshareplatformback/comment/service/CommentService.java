package com.example.musicshareplatformback.comment.service;

import com.example.musicshareplatformback.comment.dto.CreateCommentRequest;
import com.example.musicshareplatformback.community.dto.CommentSummary;

public interface CommentService {

    CommentSummary createComment(CreateCommentRequest request);
}
