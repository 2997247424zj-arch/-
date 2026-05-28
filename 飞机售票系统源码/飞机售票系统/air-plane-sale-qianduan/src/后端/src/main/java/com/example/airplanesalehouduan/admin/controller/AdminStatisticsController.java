package com.example.airplanesalehouduan.admin.controller;


import com.example.airplanesalehouduan.Login.dto.ApiResponse;
import com.example.airplanesalehouduan.admin.dto.AdminDashboardStatsResponse;
import com.example.airplanesalehouduan.admin.service.AdminStatisticsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * 管理员统计控制器
 */
@RestController
@RequestMapping({"/api/admin/statistics", "/admin/statistics"})
public class AdminStatisticsController {

    @Autowired
    private AdminStatisticsService adminStatisticsService;

    /**
     * 获取仪表板统计数据
     */
    @GetMapping("/dashboard")
    public ResponseEntity<ApiResponse<?>> getDashboardStats() {
        try {
            AdminDashboardStatsResponse stats = adminStatisticsService.getDashboardStats();
            return ResponseEntity.ok(ApiResponse.success("查询成功", stats));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * 获取近 N 天的订单趋势
     */
    @GetMapping("/order-trend")
    public ResponseEntity<ApiResponse<?>> getOrderTrend(@RequestParam(name = "period", required = false, defaultValue = "7") int period) {
        try {
            java.util.Map<String, Object> data = adminStatisticsService.getOrderTrend(period);
            return ResponseEntity.ok(ApiResponse.success("查询成功", data));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * 获取订单类型分布（国内/国际/退票/改签）
     */
    @GetMapping("/order-type-distribution")
    public ResponseEntity<ApiResponse<?>> getOrderTypeDistribution() {
        try {
            java.util.Map<String, Object> data = adminStatisticsService.getOrderTypeDistribution();
            return ResponseEntity.ok(ApiResponse.success("查询成功", data));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * 获取热门航线
     */
    @GetMapping("/top-routes")
    public ResponseEntity<ApiResponse<?>> getTopRoutes(@RequestParam(name = "limit", required = false, defaultValue = "5") int limit) {
        try {
            java.util.Map<String, Object> data = adminStatisticsService.getTopRoutes(limit);
            return ResponseEntity.ok(ApiResponse.success("查询成功", data));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * 获取运营待处理事项数量（汇总）
     */
    @GetMapping("/pending-items")
    public ResponseEntity<ApiResponse<?>> getPendingItems() {
        try {
            java.util.Map<String, Object> data = adminStatisticsService.getPendingItems();
            return ResponseEntity.ok(ApiResponse.success("查询成功", data));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * 记录待处理事项点击行为（前端埋点）
     */
    @PostMapping("/record-pending-click")
    public ResponseEntity<ApiResponse<?>> recordPendingClick(@RequestBody java.util.Map<String, Object> body) {
        try {
            // 简单记录行为（当前实现为幂等的占位实现，不产生副作用）
            // 若日后需要，可将该行为写入 DB 或消息系统
            return ResponseEntity.ok(ApiResponse.success("记录成功", java.util.Collections.emptyMap()));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * 接收前端上报的运营状态快照（示例：scheduled, delayed, cancelled 等计数）
     * 目前实现为占位：接收并返回成功，便于前端埋点正常工作
     */
    @PostMapping("/operations-snapshot")
    public ResponseEntity<ApiResponse<?>> reportOperationsSnapshot(@RequestBody java.util.Map<String, Object> body) {
        try {
            // 可选：将 body 内容写入日志或持久化，当前实现仅返回成功以避免 404
            // Example: System.out.println("Received operations snapshot: " + body);
            return ResponseEntity.ok(ApiResponse.success("上报运营快照成功", java.util.Collections.emptyMap()));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }
}

