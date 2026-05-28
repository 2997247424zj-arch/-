package com.example.airplanesalehouduan.Login.dto;

import lombok.Data;

@Data
public class EmailRegisterRequest {
    private String email;
    private String emailCode;
    private String username;
    private String password;
    private String realName;
    private String idCard;
    private String phone;
}
