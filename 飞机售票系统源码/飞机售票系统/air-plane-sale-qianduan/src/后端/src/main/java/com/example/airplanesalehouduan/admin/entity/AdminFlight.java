package com.example.airplanesalehouduan.admin.entity;

import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Data;

/**
 * 航班实体类
 * 对应数据库表 flights
 */
@Data
@Entity
@Table(name = "flights",
        uniqueConstraints = @UniqueConstraint(name = "uk_flight_no_time",
                columnNames = {"flight_no", "sched_dep_time"}))
public class AdminFlight {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "flight_no", nullable = false, length = 20)
    private String flightNo;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "aircraft_type_id", nullable = false, foreignKey = @ForeignKey(name = "fk_flights_aircraft"))
    private AdminAircraftType adminAircraftType;

    @Column(name = "origin_airport", nullable = false, length = 64)
    private String originAirport;

    @Column(name = "dest_airport", nullable = false, length = 64)
    private String destAirport;

    @Column(name = "sched_dep_time", nullable = false)
    private LocalDateTime schedDepTime;

    @Column(name = "sched_arr_time", nullable = false)
    private LocalDateTime schedArrTime;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private FlightStatus status = FlightStatus.scheduled;

    @Column(name = "route_info", columnDefinition = "JSON")
    private String routeInfo; // JSON格式的路线信息

    @Column(name = "price")
    private Double price;

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

