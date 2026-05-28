package com.example.airplanesalehouduan.Login.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * CORS 配置类
 *
 * 配置说明：
 * - 允许所有来源的跨域请求
 * - 允许所有 HTTP 方法
 * - 允许所有请求头
 * - 允许携带凭证（用于会话认证）
 *
 * 注意：本系统不使用 Token 验证，改为基于会话的认证方式
 * 因此不需要特殊的 Authorization 头处理
 */
@Configuration
public class CorsConfig {

    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                registry.addMapping("/**") // 允许所有路径
                        .allowedOriginPatterns("*") // 允许所有来源
                        .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS") // 允许的方法
                        .allowedHeaders("*") // 允许所有请求头
                        .allowCredentials(true); // 允许携带凭证（会话认证需要）
            }
        };
    }
}

