package com.example.airplanesalehouduan.Login.dto;

import lombok.Data;

@Data
public class PhoneRegisterRequest {
    private String phone;
    private String smsCode;
    private String password;
}


