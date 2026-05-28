package com.example.airplanesalehouduan.passengers.entity;



import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;
import java.time.ZoneId;

/**
 * 积分变动记录实体类（乘客隔离）
 */
@Entity
@Table(name = "loyalty_points", indexes = {
        @Index(name = "idx_passenger_time", columnList = "passenger_id,created_at")
})
@Data
public class LoyaltyPoints {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "passenger_id", nullable = false)
    private Integer passengerId;

    @Column(name = "points", nullable = false)
    private Integer points;

    @Column(name = "change_type", length = 255)
    private String changeType; // 变动类型：EARN(获得), SPEND(消费), EXCHANGE(兑换)

    @Column(name = "ref_order_id")
    private Long refOrderId; // 关联订单ID（如果是购票获得积分）

    @Column(name = "remark", length = 255)
    private String remark; // 备注说明

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        // 使用北京时间（Asia/Shanghai）创建记录
        // 明确指定时区，不依赖JVM默认时区
        // 这样无论JVM时区设置是什么，都使用北京时间
        this.createdAt = LocalDateTime.now(ZoneId.of("Asia/Shanghai"));
    }
}
