package com.example.airplanesalehouduan.Login.dto;

import lombok.Data;

/**
 * 注册请求 DTO：用于前端传递注册信息并携带短信验证码
 */
@Data
public class RegisterRequest {
    private String username;
    private String password;
    private String idCard;
    private String phone;
    private String realName;
    private String role;
    private String smsCode; // 前端传递的短信验证码
}


