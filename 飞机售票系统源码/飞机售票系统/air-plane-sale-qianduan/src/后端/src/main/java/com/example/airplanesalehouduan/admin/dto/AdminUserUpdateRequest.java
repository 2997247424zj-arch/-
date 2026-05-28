package com.example.airplanesalehouduan.admin.dto;

import lombok.Data;

/**
 * 更新用户请求DTO
 */
@Data
public class AdminUserUpdateRequest {
    private String name; // 真实姓名
    private String username; // 用户名
    private String email; // 邮箱
    private String phone; // 手机号
    private String role; // 角色
    private String department; // 部门
    private String password; // 密码（可选）
}

