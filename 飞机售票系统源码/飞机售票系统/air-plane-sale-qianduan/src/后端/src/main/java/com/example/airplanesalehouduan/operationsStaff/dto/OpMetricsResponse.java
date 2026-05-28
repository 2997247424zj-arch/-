
package com.example.airplanesalehouduan.operationsStaff.dto;

import java.util.List;
import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 运营指标响应 DTO
 * 包含 metricsStats、realtimeStatus 和可选的 onTimeHistory（准点率历史）供前端绘制趋势
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class OpMetricsResponse {
    private Map<String, Object> metricsStats;
    private Map<String, Object> realtimeStatus;
    // 支持按周期分组的历史：键如 "7d","30d","90d"
    private Map<String, List<Map<String, Object>>> onTimeHistory;
    private String lastUpdated;
}


