package com.example.airplanesalehouduan.passengers.entity;


import java.math.BigDecimal;
import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 改签申请实体类
 * 对应表 ticket_change_requests
 */
@Entity
@Table(name = "ticket_change_requests", indexes = {
        @Index(name = "idx_new_dep", columnList = "new_departure_time"),
        @Index(name = "idx_old_dep", columnList = "old_departure_time"),
        @Index(name = "idx_order", columnList = "orderno"),
        @Index(name = "idx_passenger_status", columnList = "passenger_id,status"),
        @Index(name = "idx_request_time", columnList = "request_time")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TicketChangeRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 改签编号/申请单号，用于前端展示
     */
    @Column(name = "change_no", nullable = false, length = 40, unique = true)
    private String changeNo;

    /**
     * 原订单号（冗余展示，来自 orders.order_no）
     */
    @Column(name = "orderno", nullable = false, length = 40)
    private String orderno;

    /**
     * 用户ID/申请人，关联 users(id)
     */
    @Column(name = "passenger_id", nullable = false)
    private Integer passengerId;

    /**
     * 申请人姓名（冗余展示）
     */
    @Column(name = "applicant_name", length = 50)
    private String applicantName;

    /**
     * 申请时间
     */
    @Column(name = "request_time")
    private LocalDateTime requestTime;

    /**
     * 机票号（展示用，暂时可以为空）
     */
    @Column(name = "ticket_no", length = 40)
    private String ticketNo;

    /**
     * 旧航班号（字符）
     */
    @Column(name = "old_flight_no", nullable = false, length = 20)
    private String oldFlightNo;

    /**
     * 旧航线，例：北京 → 上海
     */
    @Column(name = "old_route", nullable = false, length = 100)
    private String oldRoute;

    /**
     * 旧航线起飞时间
     */
    @Column(name = "old_departure_time", nullable = false)
    private LocalDateTime oldDepartureTime;

    /**
     * 新航班号（字符）
     */
    @Column(name = "new_flight_no", nullable = false, length = 20)
    private String newFlightNo;

    /**
     * 新航线
     */
    @Column(name = "new_route", nullable = false, length = 100)
    private String newRoute;

    /**
     * 新的起飞时间
     */
    @Column(name = "new_departure_time", nullable = false)
    private LocalDateTime newDepartureTime;

    /**
     * 改签费
     */
    @Column(name = "change_fee", precision = 10, scale = 2)
    private BigDecimal changeFee;

    /**
     * 差价（正数需补、负数应退）
     */
    @Column(name = "fare_diff", precision = 10, scale = 2)
    private BigDecimal fareDiff;

    /**
     * 改签状态（中文字符串：待处理/通过/拒绝 等）
     */
    @Column(name = "status", nullable = false, length = 100)
    private String status;

    /**
     * 申请原因
     */
    @Column(name = "reason", columnDefinition = "TEXT")
    private String reason;

    /**
     * 运营处理人ID，关联 users(id)
     */
    @Column(name = "processed_by")
    private Integer processedBy;

    /**
     * 处理时间
     */
    @Column(name = "processed_at")
    private LocalDateTime processedAt;

    /**
     * 运营备注
     */
    @Column(name = "remark", columnDefinition = "TEXT")
    private String remark;

    /**
     * 新订单号（改签生成的新订单）
     */
    @Column(name = "new_order_no", length = 40)
    private String newOrderNo;

    @PrePersist
    protected void onCreate() {
        if (requestTime == null) {
            requestTime = LocalDateTime.now();
        }
    }
}



