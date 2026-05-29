package com.example.vuecourseproject.cashier.controller;

import com.example.vuecourseproject.cashier.service.OrderStatisticService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/statistics")
@RequiredArgsConstructor
@Slf4j
public class OrderStatisticController {

    private final OrderStatisticService statisticService;

    @GetMapping("/monthly")
    public ResponseEntity<?> getMonthlyStats(
            @RequestParam Integer year,
            @RequestParam String cashierName) {

        log.info("获取月度统计 - 年份: {}, 收银员: {}", year, cashierName);

        try {
            if (cashierName == null || cashierName.trim().isEmpty()) {
                throw new IllegalArgumentException("收银员姓名不能为空");
            }

            Map<String, Object> stats = statisticService.getMonthlyStats(year, cashierName.trim());
            List<Map<String, Object>> monthlyData =
                    statisticService.getMonthlyStatsGroupByMonth(year, cashierName.trim());

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "data", monthlyData != null ? monthlyData : List.of(),
                    "summary", stats != null ? stats : Map.of(
                            "totalSales", 0,
                            "orderCount", 0,
                            "totalDiscount", 0
                    )
            ));

        } catch (Exception e) {
            log.error("获取统计信息失败", e);
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", e.getMessage()
            ));
        }
    }
}