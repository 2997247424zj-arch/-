package com.example.musicshareplatformback.auth.service;

import com.example.musicshareplatformback.auth.dto.LoginRequest;
import com.example.musicshareplatformback.auth.dto.LoginResponse;
import com.example.musicshareplatformback.auth.dto.RegisterRequest;

public interface AuthService {

    LoginResponse login(LoginRequest request);

    LoginResponse register(RegisterRequest request);
}
