package com.example.ecommerceback.utils;

import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

public class PasswordEncoderTest {

    @Test
    public void generatePassword() {
        PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
        String rawPassword = "123456";
        String encodedPassword = passwordEncoder.encode(rawPassword);
        
        System.out.println("原始密码: " + rawPassword);
        System.out.println("加密后密码: " + encodedPassword);
        System.out.println("验证密码: " + passwordEncoder.matches(rawPassword, encodedPassword));
        
        // 测试数据库中的密码
        String dbPassword = "$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy";
        System.out.println("\n测试数据库密码:");
        System.out.println("数据库密码: " + dbPassword);
        System.out.println("验证结果: " + passwordEncoder.matches(rawPassword, dbPassword));
    }
}
