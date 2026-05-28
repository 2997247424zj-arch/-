package com.example.ecommerceback.utils;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

/**
 * 密码生成工具类
 * 用于生成 BCrypt 加密的密码
 */
public class PasswordGenerator {

    public static void main(String[] args) {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

        // 生成常用测试密码的 BCrypt 加密版本
        String[] passwords = {"password123", "123456", "admin", "test"};

        System.out.println("=".repeat(60));
        System.out.println("BCrypt 密码生成工具");
        System.out.println("=".repeat(60));

        for (String password : passwords) {
            String encoded = encoder.encode(password);
            System.out.println("\n原始密码: " + password);
            System.out.println("加密后:   " + encoded);
            System.out.println("-".repeat(60));
        }

        System.out.println("\n使用方法:");
        System.out.println("1. 复制上面的加密密码");
        System.out.println("2. 在数据库中执行:");
        System.out.println("   UPDATE user SET password = '加密密码' WHERE username = '用户名';");
        System.out.println("\n或者使用提供的 update-passwords.sql 脚本");
        System.out.println("=".repeat(60));
    }
}