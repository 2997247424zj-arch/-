package com.example.airplanesalehouduan.operationsStaff.controller;

import com.example.airplanesalehouduan.Login.dto.ApiResponse;
import com.example.airplanesalehouduan.operationsStaff.service.OpMetricsService;
import com.example.airplanesalehouduan.operationsStaff.dto.OpMetricsResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

/**
 * 运营指标控制器（为运营视图提供专用接口，避免修改现有 admin 接口）
 *
 * 新增端点：GET /api/operations/metrics
 */
@RestController
@RequestMapping({"/api/operations/metrics", "/operations/metrics"})
public class OpMetricsController {

    @Autowired
    private OpMetricsService opMetricsService;

    @GetMapping("")
    public ResponseEntity<ApiResponse<?>> getMetrics() {
        try {
            OpMetricsResponse data = opMetricsService.getLatestMetrics();
            return ResponseEntity.ok(ApiResponse.success("查询成功", data));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * Server-Sent Events 实时流：当服务端聚合到新指标时会推送到已连接的客户端。
     * 路径：GET /api/operations/metrics/stream
     *
     * 此接口不会改变现有同步接口，且为弱订阅（连接断开自动清理）。
     */
    @GetMapping("/stream")
    public SseEmitter streamMetrics() {
        // 设置为无限超时（前端可自行重连）
        SseEmitter emitter = new SseEmitter(0L);
        try {
            opMetricsService.registerEmitter(emitter);
        } catch (Exception e) {
            try { emitter.completeWithError(e); } catch (Exception ignore) {}
        }
        return emitter;
    }
}


