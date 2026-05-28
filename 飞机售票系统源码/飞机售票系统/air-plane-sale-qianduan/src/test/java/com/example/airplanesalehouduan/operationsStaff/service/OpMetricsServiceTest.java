package com.example.airplanesalehouduan.operationsStaff.service;

import com.example.airplanesalehouduan.operationsStaff.dto.OpMetricsResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.jdbc.core.JdbcTemplate;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import java.util.HashMap;

class OpMetricsServiceTest {

    private JdbcTemplate jdbcTemplate;
    private OpMetricsService metricsService;

    @BeforeEach
    void setup() {
        jdbcTemplate = Mockito.mock(JdbcTemplate.class);
        metricsService = new OpMetricsService(jdbcTemplate);

        // 默认所有 count 查询返回 1 或 0，便于验证计算逻辑不会抛异常
        when(jdbcTemplate.queryForObject(anyString(), Mockito.eq(Integer.class)))
                .thenReturn(1);
    }

    @Test
    void testGetLatestMetrics_returnsResponse() {
        OpMetricsResponse resp = metricsService.getLatestMetrics();
        assertNotNull(resp);
        assertNotNull(resp.getMetricsStats());
        assertNotNull(resp.getRealtimeStatus());
        // lastUpdated 应当被填充
        assertNotNull(resp.getLastUpdated());
        // metricsStats 中应包含 onTimeRate 字段（数值或 0）
        Object onTime = resp.getMetricsStats().get("onTimeRate");
        assertNotNull(onTime);
    }
}


