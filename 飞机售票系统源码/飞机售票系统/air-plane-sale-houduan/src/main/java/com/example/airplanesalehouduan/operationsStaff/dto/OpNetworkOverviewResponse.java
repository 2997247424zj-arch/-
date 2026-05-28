package com.example.airplanesalehouduan.operationsStaff.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;
import java.util.HashMap;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OpNetworkOverviewResponse {
    private Map<String, Object> networkStats;
    private List<Map<String, Object>> topRoutes;
    // 支持按周期返回的航线收益（键如 "7d","30d","90d" -> 每个为 List{ label, value }）
    private Map<String, List<Map<String, Object>>> routeRevenue;
    private List<Map<String, Object>> networkCities;
    private String lastUpdated;
}


