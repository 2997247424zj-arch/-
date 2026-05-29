package com.example.vuecourseproject.cashier.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public interface OrderStatisticService {
    // 修改为统一的参数类型
    Map<String, Object> getMonthlyStats(Integer year, String cashierName);

    List<Map<String, Object>> getMonthlyStatsGroupByMonth(
            Integer year,
            String cashierName
    );
}