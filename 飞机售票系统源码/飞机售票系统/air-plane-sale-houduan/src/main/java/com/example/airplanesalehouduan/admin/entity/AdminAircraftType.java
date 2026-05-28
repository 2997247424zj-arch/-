package com.example.airplanesalehouduan.admin.entity;

import jakarta.persistence.*;
import lombok.Data;

/**
 * 机型实体类
 * 对应数据库表 aircraft_types
 */
@Data
@Entity
@Table(name = "aircraft_types")
public class AdminAircraftType {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "type_code", nullable = false, unique = true, length = 20)
    private String typeCode;

    @Column(length = 50)
    private String manufacturer;

    @Column(length = 50)
    private String model;

    @Column(name = "seat_layout", columnDefinition = "JSON")
    private String seatLayout; // JSON格式的座位布局

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Status status = Status.active;

    public enum Status {
        active, retired
    }
}

