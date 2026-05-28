package com.example.airplanesalehouduan.Login.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

/**
 * 用户实体类
 *
 * 认证说明：
 * - 本系统不使用 Token 验证
 * - 用户信息仅在登录时返回，前端存储在 sessionStorage
 * - 不需要存储 token 字段
 */
@Data // Lombok注解，自动生成getter, setter, toString等方法
@Entity
@Table(name = "users") // 映射到数据库的 'users' 表
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false, unique = true, length = 50)
    private String username;

    @Column(nullable = false, length = 255)
    private String password; // 注意：此处存储的是明文密码，不安全

    @Column(name = "id_card", unique = true, length = 20)
    private String idCard;

    @Column(length = 15)
    private String phone;

    @Column(unique = true, length = 100)
    private String email;

    @Column(name = "real_name", length = 50)
    private String realName;

    @Column(nullable = false, length = 20)
    private String role = "passenger";

    @Column(nullable = false, length = 20)
    private String status = "active";

    @CreationTimestamp // 自动在创建时设置时间
    @Column(name = "registration_time", updatable = false)
    private LocalDateTime registrationTime;

    @UpdateTimestamp // 自动在更新时设置时间
    @Column(name = "updated_time")
    private LocalDateTime updatedTime;

    public Object getRegisterTime() {
        return registrationTime;
    }
}

