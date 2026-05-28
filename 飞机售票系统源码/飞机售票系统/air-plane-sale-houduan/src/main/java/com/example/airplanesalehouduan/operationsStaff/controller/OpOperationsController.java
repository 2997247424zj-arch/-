package com.example.airplanesalehouduan.operationsStaff.controller;

import com.example.airplanesalehouduan.operationsStaff.dto.OpFlightPlanDto;
import com.example.airplanesalehouduan.operationsStaff.dto.OpStatusDistributionDto;
import com.example.airplanesalehouduan.operationsStaff.service.OpOperationsService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 航空运营控制台接口（仅 @operationsStaff 使用）
 *
 * 对接前端：今日航班状态分布 + 今日航班计划
 * URL 前缀：/op/operations/flights/...
 */
@RestController
// 注意：前端基地址默认为 http://localhost:3000/api
// 因此前缀中包含 /api，避免 404
@RequestMapping("/api/op/operations/flights")
public class OpOperationsController {

    private final OpOperationsService operationsService;

    public OpOperationsController(OpOperationsService operationsService) {
        this.operationsService = operationsService;
    }

    /**
     * 今日航班状态分布
     * GET /op/operations/flights/status-distribution?planDate=2025-12-09
     */
    @GetMapping("/status-distribution")
    public Map<String, Object> getStatusDistribution(
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate planDate) {

        OpStatusDistributionDto dto = operationsService.getStatusDistribution(planDate);
        Map<String, Object> data = new HashMap<>();
        data.put("statusCounts", dto);

        Map<String, Object> resp = new HashMap<>();
        resp.put("success", true);
        resp.put("data", data);
        return resp;
    }

    /**
     * 今日航班计划列表
     * GET /op/operations/flights/plans?planDate=2025-12-09
     */
    @GetMapping("/plans")
    public Map<String, Object> getPlans(
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate planDate) {
        // 如果不传 planDate，则返回所有计划（后端处理）
        List<OpFlightPlanDto> plans = operationsService.getPlans(planDate);

        Map<String, Object> data = new HashMap<>();
        data.put("plans", plans);

        Map<String, Object> resp = new HashMap<>();
        resp.put("success", true);
        resp.put("data", data);
        return resp;
    }
}
