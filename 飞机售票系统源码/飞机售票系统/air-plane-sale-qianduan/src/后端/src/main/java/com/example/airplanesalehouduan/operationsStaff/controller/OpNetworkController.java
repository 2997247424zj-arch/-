package com.example.airplanesalehouduan.operationsStaff.controller;

import com.example.airplanesalehouduan.Login.dto.ApiResponse;
import com.example.airplanesalehouduan.operationsStaff.service.OpNetworkService;
import com.example.airplanesalehouduan.operationsStaff.dto.OpNetworkOverviewResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 提供航线网络相关的轻量接口，供前端 `AnalyticsView` 调用。
 *
 * - GET  /api/operations/network/overview    -> 返回网络概览（networkStats/topRoutes/routeRevenue/networkCities）
 * - GET  /api/operations/network/top-routes  -> 返回仅 topRoutes，支持 limit 参数
 *
 * 设计原则：最小侵入，返回与前端期望的 JSON 结构兼容。
 */
@RestController
@RequestMapping({"/api/operations/network", "/operations/network"})
public class OpNetworkController {

    @Autowired
    private OpNetworkService opNetworkService;

    @GetMapping("/overview")
    public ResponseEntity<ApiResponse<?>> getOverview() {
        try {
            OpNetworkOverviewResponse data = opNetworkService.getOverview();
            return ResponseEntity.ok(ApiResponse.success("查询成功", data));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @GetMapping("/top-routes")
    public ResponseEntity<ApiResponse<?>> getTopRoutes(
            @RequestParam(name = "page", required = false, defaultValue = "0") int page,
            @RequestParam(name = "size", required = false, defaultValue = "5") int size,
            @RequestParam(name = "sort", required = false, defaultValue = "revenue") String sort
    ) {
        try {
            Map<String, Object> data = opNetworkService.getTopRoutesPaginated(page, size, sort);
            return ResponseEntity.ok(ApiResponse.success("查询成功", data));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }
}