package com.example.airplanesalehouduan.admin.dto;



import lombok.Data;

/**
 * 创建用户请求DTO
 */
@Data
public class AdminUserCreateRequest {
    private String name; // 真实姓名
    private String username; // 用户名
    private String phone; // 手机号
    private String role; // 角色
    private String password; // 密码
    private String idCard;//身份证
}

