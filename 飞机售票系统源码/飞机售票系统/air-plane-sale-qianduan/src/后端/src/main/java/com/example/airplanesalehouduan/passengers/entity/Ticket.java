package com.example.airplanesalehouduan.passengers.entity;

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
 * 机票实体类
 * 对应数据库表 tickets
 * 每个乘客一张机票，支持一个订单多张机票
 */
@Data
@Entity(name = "PassengerTicket")
@Table(name = "tickets",
        uniqueConstraints = @UniqueConstraint(name = "uk_ticket_no", columnNames = {"ticket_no"}),
        indexes = {
                @Index(name = "idx_order_no", columnList = "order_no"),
                @Index(name = "idx_passenger_id", columnList = "passenger_id"),
                @Index(name = "idx_passenger_name", columnList = "passenger_name"),
                @Index(name = "idx_id_card", columnList = "id_card"),
                @Index(name = "idx_flight_id", columnList = "flight_id"),
                @Index(name = "idx_flight_no", columnList = "flight_no"),
                @Index(name = "idx_departure_time", columnList = "departure_time"),
                @Index(name = "idx_status", columnList = "status"),
                @Index(name = "idx_created_at", columnList = "created_at"),
                @Index(name = "idx_ticket_no", columnList = "ticket_no"),
                @Index(name = "idx_passenger_flight", columnList = "passenger_id,flight_id"),
                @Index(name = "idx_order_passenger", columnList = "order_no,passenger_id")
        })
public class Ticket {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "order_no", nullable = false, length = 40)
    private String orderNo; // 订单号，关联orders(order_no)

    @Column(name = "passenger_id", nullable = false)
    private Integer passengerId; // 乘客ID（关联users表，实现数据隔离）

    @Column(name = "passenger_name", nullable = false, length = 50)
    private String passengerName; // 乘客姓名（冗余字段，便于查询）

    @Column(name = "id_card", nullable = false, length = 20)
    private String idCard; // 身份证号

    @Column(length = 15)
    private String phone; // 联系电话

    @Column(name = "flight_id", nullable = false)
    private Integer flightId; // 航班ID，关联flights(id)

    @Column(name = "flight_no", nullable = false, length = 20)
    private String flightNo; // 航班号

    @Column(name = "origin_airport", nullable = false, length = 50)
    private String originAirport; // 出发机场

    @Column(name = "dest_airport", nullable = false, length = 50)
    private String destAirport; // 到达机场

    @Column(length = 100)
    private String route; // 航线（格式：出发地 → 目的地，如：北京 → 上海）

    @Column(name = "departure_time", nullable = false)
    private LocalDateTime departureTime; // 起飞时间

    @Column(name = "arrival_time", nullable = false)
    private LocalDateTime arrivalTime; // 到达时间

    @Column(name = "seat_number", length = 10)
    private String seatNumber; // 座位号，如：10A, 12B等

    @Column(name = "seat_class", length = 20)
    private String seatClass; // 舱位等级（economy-经济舱, business-商务舱, first-头等舱）

    @Column(name = "base_price", nullable = false, precision = 10, scale = 2)
    private BigDecimal basePrice = BigDecimal.ZERO; // 基础票价

    @Column(name = "seat_fee", nullable = false, precision = 10, scale = 2)
    private BigDecimal seatFee = BigDecimal.ZERO; // 座位选择费

    @Column(name = "discount_fee", nullable = false, precision = 10, scale = 2)
    private BigDecimal discountFee = BigDecimal.ZERO; // 优惠费（如有）

    @Column(name = "total_price", nullable = false, precision = 10, scale = 2)
    private BigDecimal totalPrice = BigDecimal.ZERO; // 机票总价（基础票价 + 座位费 - 优惠费）

    @Column(name = "ticket_no", nullable = false, unique = true, length = 50)
    private String ticketNo; // 机票号（唯一标识，格式：如TKT+时间戳+随机数）

    @Column(nullable = false, length = 20)
    private String status; // 机票状态

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt; // 创建时间

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt; // 更新时间
}

