package com.example.airplanesalehouduan.passengers.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Type;

import java.time.LocalDateTime;

/**
 * 重点旅客预约实体类
 */
@Entity
@Table(name = "special_service_requests", indexes = {
        @Index(name = "idx_passenger", columnList = "passenger_id"),
        @Index(name = "idx_order", columnList = "order_no"),
        @Index(name = "idx_status", columnList = "status"),
        @Index(name = "idx_passenger_status", columnList = "passenger_id,status"),
        @Index(name = "idx_created_at", columnList = "created_at")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SpecialServiceRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 乘客ID（关联users表，实现数据隔离）
     */
    @Column(name = "passenger_id", nullable = false)
    private Integer passengerId;

    /**
     * 订单号
     */
    @Column(name = "order_no", nullable = false, length = 200)
    private String orderNo;

    /**
     * 联系电话
     */
    @Column(name = "phone", nullable = false, length = 15)
    private String phone;

    /**
     * 旅客类型
     */
    @Column(name = "passenger_type", nullable = false, length = 50)
    private String passengerType;

    /**
     * 出发机场
     */
    @Column(name = "departure_airport", nullable = false, length = 100)
    private String departureAirport;

    /**
     * 到达机场
     */
    @Column(name = "arrival_airport", nullable = false, length = 100)
    private String arrivalAirport;

    /**
     * 进站服务需求（JSON格式）
     */
    @Column(name = "entry_services", columnDefinition = "JSON")
    private String entryServices;

    /**
     * 出站服务需求（JSON格式）
     */
    @Column(name = "exit_services", columnDefinition = "JSON")
    private String exitServices;

    /**
     * 情况描述
     */
    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    /**
     * 申请状态
     * pending-待处理、approved-已批准、processing-处理中、completed-已完成、rejected-已拒绝、cancelled-已取消
     */
    @Column(name = "status", length = 20)
    private String status = "pending";

    /**
     * 创建时间
     */
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}

