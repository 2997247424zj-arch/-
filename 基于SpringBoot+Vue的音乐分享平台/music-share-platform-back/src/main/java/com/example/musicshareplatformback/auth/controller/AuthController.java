package com.example.musicshareplatformback.auth.controller;

import com.example.musicshareplatformback.auth.dto.LoginRequest;
import com.example.musicshareplatformback.auth.dto.LoginResponse;
import com.example.musicshareplatformback.auth.dto.RegisterRequest;
import com.example.musicshareplatformback.auth.service.AuthService;
import com.example.musicshareplatformback.common.ApiResponse;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public ApiResponse<LoginResponse> login(@RequestBody LoginRequest request) {
        return ApiResponse.success("Login succeeded.", authService.login(request));
    }

    @PostMapping("/register")
    public ApiResponse<LoginResponse> register(@RequestBody RegisterRequest request) {
        return ApiResponse.success("Register succeeded.", authService.register(request));
    }
}
