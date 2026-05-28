package com.example.musicshareplatformback.auth.controller;

import com.example.musicshareplatformback.auth.service.AuthService;
import com.example.musicshareplatformback.auth.service.impl.StaticAuthService;
import com.example.musicshareplatformback.common.GlobalExceptionHandler;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class AuthControllerTest {

    @Test
    void shouldLoginWithDemoUser() throws Exception {
        AuthService authService = new StaticAuthService();
        MockMvc mockMvc = MockMvcBuilders.standaloneSetup(new AuthController(authService))
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "username": "demo_user",
                                  "password": "123456"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.profile.role").value("USER"));
    }

    @Test
    void shouldRejectWrongPassword() throws Exception {
        AuthService authService = new StaticAuthService();
        MockMvc mockMvc = MockMvcBuilders.standaloneSetup(new AuthController(authService))
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "username": "demo_user",
                                  "password": "wrong"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    void shouldRegisterNewCreator() throws Exception {
        AuthService authService = new StaticAuthService();
        MockMvc mockMvc = MockMvcBuilders.standaloneSetup(new AuthController(authService))
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "username": "new_creator",
                                  "password": "123456",
                                  "displayName": "New Creator",
                                  "role": "CREATOR",
                                  "favoriteGenre": "Indie",
                                  "bio": "正在上传自己的第一批作品。"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.profile.role").value("CREATOR"))
                .andExpect(jsonPath("$.data.capabilities[4]").value("songs:upload"));
    }

    @Test
    void shouldRejectDuplicateRegisterUsername() throws Exception {
        AuthService authService = new StaticAuthService();
        MockMvc mockMvc = MockMvcBuilders.standaloneSetup(new AuthController(authService))
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "username": "demo_user",
                                  "password": "123456",
                                  "displayName": "Duplicate User",
                                  "role": "USER",
                                  "favoriteGenre": "Lo-fi"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }
}
