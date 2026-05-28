package com.example.airplanesalehouduan.operationsStaff.dto;

import com.example.airplanesalehouduan.operationsStaff.entity.OpFlight;
import com.example.airplanesalehouduan.operationsStaff.entity.OpFlightPlan;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 今日航班计划列表 DTO（对接前端“今日航班计划”模块）
 */
public class OpFlightPlanDto {

    private Integer id;
    private String flightNo;
    private String originAirport;
    private String destAirport;
    private LocalDateTime schedDepTime;
    private LocalDateTime schedArrTime;
    private LocalDate planDate;
    private String gate;
    private String runway;
    private String crewInfo;
    private String status;

    public static OpFlightPlanDto from(OpFlightPlan plan) {
        OpFlight flight = plan.getFlight();
        OpFlightPlanDto dto = new OpFlightPlanDto();
        dto.id = plan.getId();
        dto.flightNo = flight != null ? flight.getFlightNo() : null;
        dto.originAirport = flight != null ? flight.getOriginAirport() : null;
        dto.destAirport = flight != null ? flight.getDestAirport() : null;
        dto.schedDepTime = flight != null ? flight.getSchedDepTime() : null;
        dto.schedArrTime = flight != null ? flight.getSchedArrTime() : null;
        dto.planDate = plan.getPlanDate();
        dto.gate = plan.getGate();
        dto.runway = plan.getRunway();
        dto.crewInfo = plan.getCrewInfo();
        dto.status = plan.getStatus() != null
                ? plan.getStatus()
                : (flight != null && flight.getStatus() != null ? flight.getStatus().name() : null);
        return dto;
    }

    public Integer getId() {
        return id;
    }

    public String getFlightNo() {
        return flightNo;
    }

    public String getOriginAirport() {
        return originAirport;
    }

    public String getDestAirport() {
        return destAirport;
    }

    public LocalDateTime getSchedDepTime() {
        return schedDepTime;
    }

    public LocalDateTime getSchedArrTime() {
        return schedArrTime;
    }

    public LocalDate getPlanDate() {
        return planDate;
    }

    public String getGate() {
        return gate;
    }

    public String getRunway() {
        return runway;
    }

    public String getCrewInfo() {
        return crewInfo;
    }

    public String getStatus() {
        return status;
    }
}
