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
 * 取消机票申请实体类
 * 对应表 ticket_cancel_requests
 */
@Entity
@Table(name = "ticket_cancel_requests", indexes = {
        @Index(name = "idx_order", columnList = "orderno"),
        @Index(name = "idx_passenger_status", columnList = "passenger_id,status"),
        @Index(name = "idx_request_time", columnList = "request_time"),
        @Index(name = "idx_new_dep", columnList = "departure_time")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TicketCancelRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 取消编号/申请单号，用于前端展示
     */
    @Column(name = "cancel_no", nullable = false, length = 40, unique = true)
    private String cancelNo;

    /**
     * 订单号（冗余展示，来自 orders.order_no）
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
     * 航班号
     */
    @Column(name = "flight_no", nullable = false, length = 20)
    private String flightNo;

    /**
     * 航线，例：北京 → 上海
     */
    @Column(name = "route", nullable = false, length = 100)
    private String route;

    /**
     * 航班起飞时间
     */
    @Column(name = "departure_time", nullable = false)
    private LocalDateTime departureTime;

    /**
     * 取消手续费
     */
    @Column(name = "cancel_fee", precision = 10, scale = 2)
    private BigDecimal cancelFee;

    /**
     * 退款金额（票价 - 手续费）
     */
    @Column(name = "refund_fare", precision = 10, scale = 2)
    private BigDecimal refundFare;

    /**
     * 取消状态（中文字符串：待处理/通过/拒绝 等）
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

    @PrePersist
    protected void onCreate() {
        if (requestTime == null) {
            requestTime = LocalDateTime.now();
        }
    }
}



