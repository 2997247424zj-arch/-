package com.example.airplanesalehouduan.admin.dto;

import java.time.LocalDateTime;

import lombok.Data;

/**
 * 创建航班请求DTO
 */
@Data
public class AdminFlightCreateRequestDTO {
    private String flightNo; // 航班号
    private Integer aircraftTypeId; // 机型ID
    private String aircraftModel; // 机型名称/型号（回退情况）
    private Double price; // 票价（可选）
    private String originAirport; // 出发机场
    private String destAirport; // 目的地机场
    private LocalDateTime schedDepTime; // 计划出发时间
    private LocalDateTime schedArrTime; // 计划到达时间
    private String status; // 状态: scheduled, delayed, cancelled, boarding, departed, arrived
    private String routeInfo; // 路线信息JSON
}

