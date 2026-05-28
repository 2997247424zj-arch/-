package com.example.airplanesalehouduan.passengers.entity;


import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

/**
 * 机型实体类
 * 对应数据库表 aircraft_types
 */
@Data
@Entity(name = "PassengerAircraftType")
@Table(name = "aircraft_types")
public class AircraftType {

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


