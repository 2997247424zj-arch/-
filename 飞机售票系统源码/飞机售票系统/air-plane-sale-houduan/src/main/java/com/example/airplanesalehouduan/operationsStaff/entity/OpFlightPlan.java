package com.example.airplanesalehouduan.operationsStaff.entity;

import jakarta.persistence.*;

import java.time.LocalDate;

/**
 * 今日航班计划 / 运行计划（对应 flight_plans 表）
 */
@Entity
@Table(name = "flight_plans")
public class OpFlightPlan {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "flight_id", nullable = false)
    private Integer flightId;

    @Column(name = "plan_date", nullable = false)
    private LocalDate planDate;

    @Column(name = "gate", length = 10)
    private String gate;

    @Column(name = "runway", length = 10)
    private String runway;

    @Column(name = "crew_info", columnDefinition = "json")
    private String crewInfo;

    @Column(name = "status", length = 10)
    private String status;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "flight_id", insertable = false, updatable = false)
    private OpFlight flight;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getFlightId() {
        return flightId;
    }

    public void setFlightId(Integer flightId) {
        this.flightId = flightId;
    }

    public LocalDate getPlanDate() {
        return planDate;
    }

    public void setPlanDate(LocalDate planDate) {
        this.planDate = planDate;
    }

    public String getGate() {
        return gate;
    }

    public void setGate(String gate) {
        this.gate = gate;
    }

    public String getRunway() {
        return runway;
    }

    public void setRunway(String runway) {
        this.runway = runway;
    }

    public String getCrewInfo() {
        return crewInfo;
    }

    public void setCrewInfo(String crewInfo) {
        this.crewInfo = crewInfo;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public OpFlight getFlight() {
        return flight;
    }

    public void setFlight(OpFlight flight) {
        this.flight = flight;
    }
}
