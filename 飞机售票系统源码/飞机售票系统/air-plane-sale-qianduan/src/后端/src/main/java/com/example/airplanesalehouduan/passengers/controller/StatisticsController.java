package com.example.airplanesalehouduan.passengers.controller;



import com.example.airplanesalehouduan.Login.dto.ApiResponse;

import com.example.airplanesalehouduan.passengers.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 统计数据控制器
 * 提供乘客订单相关的各种统计功能
 */
@RestController
@RequestMapping({"/api/passenger", "/passenger"})
public class StatisticsController {

    @Autowired
    private OrderService orderService;

    /**
     * 获取完整的统计数据
     * GET /api/passenger/statistics?passengerId=xxx
     */
    @GetMapping("/statistics")
    public ResponseEntity<ApiResponse<?>> getStatistics(
            @RequestParam(required = true) Integer passengerId) {
        try {
            Map<String, Object> statistics = orderService.getCompleteStatistics(passengerId);
            return ResponseEntity.ok(ApiResponse.success("查询成功", statistics));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("获取统计数据失败: " + e.getMessage()));
        }
    }

    /**
     * 获取消费趋势数据（近6个月）
     * GET /api/passenger/statistics/spending-trend?passengerId=xxx
     */
    @GetMapping("/statistics/spending-trend")
    public ResponseEntity<ApiResponse<?>> getSpendingTrend(
            @RequestParam(required = true) Integer passengerId) {
        try {
            List<Map<String, Object>> trend = orderService.getSpendingTrend(passengerId);
            Map<String, Object> responseData = new HashMap<>();
            responseData.put("trend", trend);
            return ResponseEntity.ok(ApiResponse.success("查询成功", responseData));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("获取消费趋势失败: " + e.getMessage()));
        }
    }

    /**
     * 获取月度对比数据（订单数 vs 消费额）
     * GET /api/passenger/statistics/monthly-comparison?passengerId=xxx
     */
    @GetMapping("/statistics/monthly-comparison")
    public ResponseEntity<ApiResponse<?>> getMonthlyComparison(
            @RequestParam(required = true) Integer passengerId) {
        try {
            List<Map<String, Object>> comparison = orderService.getMonthlyComparison(passengerId);
            Map<String, Object> responseData = new HashMap<>();
            responseData.put("comparison", comparison);
            return ResponseEntity.ok(ApiResponse.success("查询成功", responseData));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("获取月度对比失败: " + e.getMessage()));
        }
    }

    /**
     * 获取订单状态分布
     * GET /api/passenger/statistics/status-distribution?passengerId=xxx
     */
    @GetMapping("/statistics/status-distribution")
    public ResponseEntity<ApiResponse<?>> getStatusDistribution(
            @RequestParam(required = true) Integer passengerId) {
        try {
            List<Map<String, Object>> distribution = orderService.getOrderStatusDistribution(passengerId);
            Map<String, Object> responseData = new HashMap<>();
            responseData.put("distribution", distribution);
            return ResponseEntity.ok(ApiResponse.success("查询成功", responseData));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("获取状态分布失败: " + e.getMessage()));
        }
    }

    /**
     * 获取热门航线
     * GET /api/passenger/statistics/popular-routes?passengerId=xxx&limit=5
     */
    @GetMapping("/statistics/popular-routes")
    public ResponseEntity<ApiResponse<?>> getPopularRoutes(
            @RequestParam(required = true) Integer passengerId,
            @RequestParam(defaultValue = "5") int limit) {
        try {
            List<Map<String, Object>> routes = orderService.getPopularRoutes(passengerId, limit);
            Map<String, Object> responseData = new HashMap<>();
            responseData.put("routes", routes);
            return ResponseEntity.ok(ApiResponse.success("查询成功", responseData));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("获取热门航线失败: " + e.getMessage()));
        }
    }

    /**
     * 获取所有统计数据（一次性返回所有统计信息）
     * GET /api/passenger/statistics/all?passengerId=xxx
     */
    @GetMapping("/statistics/all")
    public ResponseEntity<ApiResponse<?>> getAllStatistics(
            @RequestParam(required = true) Integer passengerId) {
        try {
            Map<String, Object> allStats = new HashMap<>();

            allStats.put("basic", orderService.getCompleteStatistics(passengerId));

            Map<String, Object> trendData = new HashMap<>();
            trendData.put("trend", orderService.getSpendingTrend(passengerId));
            allStats.put("spendingTrend", trendData);

            Map<String, Object> comparisonData = new HashMap<>();
            comparisonData.put("comparison", orderService.getMonthlyComparison(passengerId));
            allStats.put("monthlyComparison", comparisonData);

            Map<String, Object> distributionData = new HashMap<>();
            distributionData.put("distribution", orderService.getOrderStatusDistribution(passengerId));
            allStats.put("statusDistribution", distributionData);

            Map<String, Object> routesData = new HashMap<>();
            routesData.put("routes", orderService.getPopularRoutes(passengerId, 5));
            allStats.put("popularRoutes", routesData);

            return ResponseEntity.ok(ApiResponse.success("查询成功", allStats));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("获取统计数据失败: " + e.getMessage()));
        }
    }
}
