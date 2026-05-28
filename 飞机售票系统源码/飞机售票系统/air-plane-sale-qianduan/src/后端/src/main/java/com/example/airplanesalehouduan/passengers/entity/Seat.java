package com.example.airplanesalehouduan.passengers.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 座位实体类
 * 对应数据库表 seats
 */
@Data
@Entity(name = "PassengerSeat")
@Table(name = "seats",
        uniqueConstraints = @UniqueConstraint(name = "uk_flight_seat_number",
                columnNames = {"flight_id", "seat_number"}))
public class Seat {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "flight_id", nullable = false, foreignKey = @ForeignKey(name = "fk_seats_flight"))
    private Flight flight;

    @Column(name = "seat_number", nullable = false, length = 10)
    private String seatNumber;

    @Column(name = "cabin_class", length = 30)
    private String cabinClass;

    @Column(name = "rownumber", nullable = false)
    private Integer rownumber;

    @Column(name = "seat_position", nullable = false, length = 2)
    private String seatPosition;

    @Column(name = "price", nullable = false, precision = 10, scale = 2)
    private BigDecimal price = BigDecimal.ZERO;

    @Column(name = "available_count", nullable = false)
    private Integer availableCount = 1;

    @Column(name = "status", length = 30)
    private String status; // available, occupied, reserved, maintenance

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}

