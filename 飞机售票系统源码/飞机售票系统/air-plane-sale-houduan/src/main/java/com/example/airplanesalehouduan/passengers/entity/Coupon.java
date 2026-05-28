package com.example.airplanesalehouduan.passengers.entity;


import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 优惠券实体类（乘客隔离或通用）
 */
@Entity
@Table(name = "coupons", indexes = {
        @Index(name = "idx_passenger", columnList = "passenger_id")
}, uniqueConstraints = {
        @UniqueConstraint(name = "uk_code", columnNames = "code")
})
@Data
public class Coupon {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "code", length = 40, nullable = false, unique = true)
    private String code; // 优惠券代码

    @Column(name = "passenger_id")
    private Integer passengerId; // NULL表示通用券；非空表示专属且隔离

    @Column(name = "discount_type", length = 255)
    private String discountType; // 折扣类型：FIXED(固定金额), PERCENTAGE(百分比)

    @Column(name = "discount_value", nullable = false, precision = 10, scale = 2)
    private BigDecimal discountValue; // 折扣值

    @Column(name = "min_spend", precision = 10, scale = 2)
    private BigDecimal minSpend = BigDecimal.ZERO; // 最低消费金额

    @Column(name = "valid_from", nullable = false)
    private LocalDateTime validFrom; // 有效期开始时间

    @Column(name = "valid_to", nullable = false)
    private LocalDateTime validTo; // 有效期结束时间

    @Column(name = "status", length = 255)
    private String status; // 状态：UNUSED(未领取), AVAILABLE(可用), USED(已使用), EXPIRED(已过期)

    @Column(name = "name", length = 255)
    private String name; // 优惠券名称

    @Column(name = "description", length = 500)
    private String description; // 优惠券描述
}

