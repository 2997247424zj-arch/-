package com.example.airplanesalehouduan.operationsStaff.entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;

/**
 * 航班基础信息（对应 flights 表）
 */
@Entity
@Table(name = "flights")
public class OpFlight {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "flight_no", nullable = false, length = 20)
    private String flightNo;

    @Column(name = "aircraft_type_id", nullable = false)
    private Integer aircraftTypeId;

    @Column(name = "origin_airport", nullable = false, length = 10)
    private String originAirport;

    @Column(name = "dest_airport", nullable = false, length = 10)
    private String destAirport;

    @Column(name = "sched_dep_time", nullable = false)
    private LocalDateTime schedDepTime;

    @Column(name = "sched_arr_time", nullable = false)
    private LocalDateTime schedArrTime;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private OpFlightStatus status = OpFlightStatus.scheduled;

    @Column(name = "route_info", columnDefinition = "json")
    private String routeInfo;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getFlightNo() {
        return flightNo;
    }

    public void setFlightNo(String flightNo) {
        this.flightNo = flightNo;
    }

    public Integer getAircraftTypeId() {
        return aircraftTypeId;
    }

    public void setAircraftTypeId(Integer aircraftTypeId) {
        this.aircraftTypeId = aircraftTypeId;
    }

    public String getOriginAirport() {
        return originAirport;
    }

    public void setOriginAirport(String originAirport) {
        this.originAirport = originAirport;
    }

    public String getDestAirport() {
        return destAirport;
    }

    public void setDestAirport(String destAirport) {
        this.destAirport = destAirport;
    }

    public LocalDateTime getSchedDepTime() {
        return schedDepTime;
    }

    public void setSchedDepTime(LocalDateTime schedDepTime) {
        this.schedDepTime = schedDepTime;
    }

    public LocalDateTime getSchedArrTime() {
        return schedArrTime;
    }

    public void setSchedArrTime(LocalDateTime schedArrTime) {
        this.schedArrTime = schedArrTime;
    }

    public OpFlightStatus getStatus() {
        return status;
    }

    public void setStatus(OpFlightStatus status) {
        this.status = status;
    }

    public String getRouteInfo() {
        return routeInfo;
    }

    public void setRouteInfo(String routeInfo) {
        this.routeInfo = routeInfo;
    }
}
