package com.example.airplanesalehouduan.admin.entity;


import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

/**
 * 常用乘客实体类
 * 用于存储用户添加的常用乘客信息，实现数据隔离
 */
@Data
@Entity
@Table(name = "frequent_passengers",
        uniqueConstraints = @UniqueConstraint(name = "uk_user_id_card", columnNames = {"user_id", "id_card"}))
public class AdminFrequentPassenger {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Integer userId; // 所属用户ID，用于数据隔离

    @Column(nullable = false, length = 50)
    private String name; // 乘客姓名

    @Column(name = "id_card", nullable = false, length = 18)
    private String idCard; // 身份证号（18位）

    @Column(length = 20)
    private String relationship; // 与用户的关系（配偶、子女、父母、兄弟姐妹、朋友、同事、其他）

    @Column(length = 15)
    private String phone; // 联系电话

    @Column(columnDefinition = "TEXT")
    private String remarks; // 备注信息

    @Column(name = "is_default", nullable = false)
    private Boolean isDefault = false; // 是否设为默认（0-否，1-是）

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private PassengerStatus status = PassengerStatus.active; // 状态（active-有效，deleted-已删除）

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt; // 创建时间

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt; // 更新时间

    /**
     * 乘客状态枚举
     */
    public enum PassengerStatus {
        active,   // 有效
        disabled   //禁用状态
    }
}

