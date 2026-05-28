package com.example.airplanesalehouduan.admin.entity;


import java.math.BigDecimal;
import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Data;

/**
 * 订单实体类
 * 对应数据库表 orders
 * 实现乘客数据隔离，每个订单关联到特定的乘客ID
 */
@Data
@Entity
@Table(name = "orders",
        uniqueConstraints = @UniqueConstraint(name = "uk_order_no", columnNames = {"order_no"}),
        indexes = {
                @Index(name = "idx_passenger", columnList = "passenger_id"),
                @Index(name = "idx_passenger_name", columnList = "passenger_name"),
                @Index(name = "idx_route", columnList = "route"),
                @Index(name = "idx_departure_time", columnList = "departure_time"),
                @Index(name = "idx_status", columnList = "status"),
                @Index(name = "idx_created_at", columnList = "created_at")
        })
public class AdminOrder {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "passenger_id", nullable = false)
    private Integer passengerId; // 乘客ID（关联users表，实现数据隔离）

    @Column(name = "order_no", nullable = false, unique = true, length = 40)
    private String orderNo; // 订单号

    @Column(name = "passenger_name", length = 50)
    private String passengerName; // 姓名（冗余字段，便于查询，从users.real_name同步）

    @Column(length = 100)
    private String route; // 航线（格式：出发地 → 目的地，如：北京 → 上海）

    @Column(name = "flight_no", length = 20)
    private String flightNo; // 航班号

    @Column(name = "ticket_no", length = 50)
    private String ticketNo; // 机票号

    @Column(name = "departure_time")
    private LocalDateTime departureTime; // 起飞时间

    @Column(name = "arrival_time")
    private LocalDateTime arrivalTime; // 到达时间

    @Column(name = "total_amount", nullable = false, precision = 10, scale = 2)
    private BigDecimal totalAmount; // 订单总金额

    @Column(nullable = false, length = 20, columnDefinition = "VARCHAR(20)")
    private String status; // 订单状态（字符串类型，直接使用数据库中的值）

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt; // 创建时间

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt; // 更新时间
}

