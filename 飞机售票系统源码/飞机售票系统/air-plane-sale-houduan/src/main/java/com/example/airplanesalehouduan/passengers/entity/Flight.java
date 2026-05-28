package com.example.airplanesalehouduan.passengers.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 航班实体类
 * 对应数据库表 flights
 */
@Data
@Entity(name = "PassengerFlight")
@Table(name = "flights",
        uniqueConstraints = @UniqueConstraint(name = "uk_flight_no_time",
                columnNames = {"flight_no", "sched_dep_time"}))
public class Flight {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "flight_no", nullable = false, length = 20)
    private String flightNo;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "aircraft_type_id", nullable = false, foreignKey = @ForeignKey(name = "fk_flights_aircraft"))
    private AircraftType aircraftType;

    @Column(name = "origin_airport", nullable = false, length = 10)
    private String originAirport;

    @Column(name = "dest_airport", nullable = false, length = 10)
    private String destAirport;

    @Column(name = "sched_dep_time", nullable = false)
    private LocalDateTime schedDepTime;

    @Column(name = "sched_arr_time", nullable = false)
    private LocalDateTime schedArrTime;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private FlightStatus status = FlightStatus.scheduled;

    /**
     * 基础票价（单位：元）
     * 对应表 flights.price
     */
    @Column(name = "price", precision = 10, scale = 2)
    private BigDecimal price;

    @Column(name = "route_info", columnDefinition = "JSON")
    private String routeInfo; // JSON格式的路线信息

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public enum FlightStatus {
        scheduled, delayed, cancelled, boarding, departed, arrived
    }
}


