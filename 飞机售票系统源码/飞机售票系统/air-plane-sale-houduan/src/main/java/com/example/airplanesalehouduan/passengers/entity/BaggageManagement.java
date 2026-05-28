package com.example.airplanesalehouduan.passengers.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;

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
 * 行李管理实体类
 * 对应数据库表 baggage_management
 * 实现乘客数据隔离，每个行李关联到特定的乘客ID
 */
@Data
@Entity
@Table(name = "baggage_management",
        uniqueConstraints = @UniqueConstraint(name = "uk_baggage_no", columnNames = {"baggage_no"}),
        indexes = {
                @Index(name = "idx_passenger", columnList = "passenger_id"),
                @Index(name = "idx_order", columnList = "orderno"),
                @Index(name = "idx_flight", columnList = "flight_no"),
                @Index(name = "idx_status", columnList = "status"),
                @Index(name = "idx_passenger_status", columnList = "passenger_id, status"),
                @Index(name = "idx_departure_time", columnList = "departure_time"),
                @Index(name = "idx_registered_time", columnList = "registered_time"),
                @Index(name = "idx_processed_by", columnList = "processed_by")
        })
public class BaggageManagement {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "baggage_no", nullable = false, unique = true, length = 40)
    private String baggageNo; // 行李编号/标识号

    @Column(name = "passenger_id", nullable = false)
    private Integer passengerId; // 用户ID/乘客ID，关联users(id)，实现数据隔离

    @Column(name = "passenger_name", length = 50)
    private String passengerName; // 乘客姓名（冗余展示）

    @Column(name = "orderno", nullable = false, length = 40)
    private String orderno; // 订单号，关联orders(order_no)

    @Column(name = "flight_no", nullable = false, length = 20)
    private String flightNo; // 航班号

    @Column(name = "route", nullable = false, length = 100)
    private String route; // 航线（格式：出发地 → 目的地）

    @Column(name = "departure_time", nullable = false)
    private LocalDateTime departureTime; // 起飞时间

    @Column(name = "arrival_time_flight")
    private LocalDateTime arrivalTimeFlight; // 航班到达时间

    @Column(name = "baggage_type", nullable = false, length = 50)
    private String baggageType; // 行李类型（托运行李、随身行李、超规行李、特殊行李等）

    @Column(name = "baggage_count", nullable = false)
    private Integer baggageCount = 1; // 行李数量

    @Column(name = "total_weight", precision = 10, scale = 2)
    private BigDecimal totalWeight; // 总重量（公斤）

    @Column(name = "weight_limit", precision = 10, scale = 2)
    private BigDecimal weightLimit; // 重量限制（公斤）

    @Column(name = "dimensions", length = 100)
    private String dimensions; // 尺寸（长x宽x高，单位：厘米）

    @Column(name = "status", nullable = false, length = 50)
    private String status = "registered"; // 行李状态

    @CreationTimestamp
    @Column(name = "registered_time", nullable = false, updatable = false)
    private LocalDateTime registeredTime; // 登记时间

    @Column(name = "checked_in_time")
    private LocalDateTime checkedInTime; // 托运时间

    @Column(name = "arrival_time_baggage")
    private LocalDateTime arrivalTimeBaggage; // 行李到达时间

    @Column(name = "delivered_time")
    private LocalDateTime deliveredTime; // 提取时间

    @Column(name = "baggage_fee", precision = 10, scale = 2)
    private BigDecimal baggageFee = BigDecimal.ZERO; // 行李费用

    @Column(name = "excess_fee", precision = 10, scale = 2)
    private BigDecimal excessFee = BigDecimal.ZERO; // 超重/超规费用

    @Column(name = "description", columnDefinition = "TEXT")
    private String description; // 行李描述（颜色、特征等）

    @Column(name = "remark", columnDefinition = "TEXT")
    private String remark; // 备注信息

    @Column(name = "processed_by")
    private Integer processedBy; // 运营处理人ID，关联users(id)

    @Column(name = "processed_at")
    private LocalDateTime processedAt; // 处理时间

    @Column(name = "operator_remark", columnDefinition = "TEXT")
    private String operatorRemark; // 运营备注
}

