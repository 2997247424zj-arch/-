package com.example.airplanesalehouduan.operationsStaff.controller;

import com.example.airplanesalehouduan.Login.dto.ApiResponse;
import com.example.airplanesalehouduan.passengers.entity.BaggageManagement;
import com.example.airplanesalehouduan.passengers.service.BaggageManagementService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 行李管理控制器（运营端）
 * 提供运营人员的行李管理接口
 * 可以查看和管理所有乘客的行李
 */
@RestController
@RequestMapping("/api/op")
public class OpBaggageManagementController {

    @Autowired
    private BaggageManagementService baggageService;

    /**
     * 获取所有行李列表（带筛选和分页）
     * 接口路径：GET /api/op/baggage
     *
     * @param page 页码（可选，默认0）
     * @param size 每页大小（可选，默认20）
     * @param status 状态（可选）
     * @param baggageNo 行李编号（可选，模糊查询）
     * @param orderno 订单号（可选，模糊查询）
     * @param flightNo 航班号（可选，模糊查询）
     * @param passengerName 乘客姓名（可选，模糊查询）
     * @return 行李列表和分页信息
     */
    @GetMapping("/baggage")
    public ResponseEntity<ApiResponse<?>> getAllBaggageList(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String baggageNo,
            @RequestParam(required = false) String orderno,
            @RequestParam(required = false) String flightNo,
            @RequestParam(required = false) String passengerName) {
        try {
            Page<BaggageManagement> baggagePage = baggageService.getAllBaggageList(
                    page, size, status, baggageNo, orderno, flightNo, passengerName);

            Map<String, Object> responseData = new HashMap<>();
            responseData.put("list", baggagePage.getContent());
            responseData.put("total", baggagePage.getTotalElements());
            responseData.put("page", baggagePage.getNumber());
            responseData.put("size", baggagePage.getSize());
            responseData.put("totalPages", baggagePage.getTotalPages());
            responseData.put("hasNext", baggagePage.hasNext());
            responseData.put("hasPrevious", baggagePage.hasPrevious());

            return ResponseEntity.ok(ApiResponse.success("查询成功", responseData));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("查询失败: " + e.getMessage()));
        }
    }

    /**
     * 获取行李详情
     * 接口路径：GET /api/op/baggage/{id}
     *
     * @param id 行李ID
     * @return 行李详情
     */
    @GetMapping("/baggage/{id}")
    public ResponseEntity<ApiResponse<?>> getBaggageDetail(@PathVariable Long id) {
        try {
            BaggageManagement baggage = baggageService.getBaggageDetailById(id);
            return ResponseEntity.ok(ApiResponse.success("查询成功", baggage));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * 更新行李状态
     * 接口路径：PUT /api/op/baggage/{id}/status
     *
     * @param id 行李ID
     * @param requestBody 请求体，包含status、processedBy、operatorRemark
     * @return 更新后的行李记录
     */
    @PutMapping("/baggage/{id}/status")
    public ResponseEntity<ApiResponse<?>> updateBaggageStatus(
            @PathVariable Long id,
            @RequestBody Map<String, Object> requestBody) {
        try {
            String status = (String) requestBody.get("status");
            Integer processedBy = requestBody.get("processedBy") != null ?
                    Integer.valueOf(requestBody.get("processedBy").toString()) : null;
            String operatorRemark = (String) requestBody.get("operatorRemark");

            if (status == null || status.trim().isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(ApiResponse.error("状态不能为空"));
            }

            BaggageManagement baggage = baggageService.updateBaggageStatus(
                    id, status, processedBy, operatorRemark);
            return ResponseEntity.ok(ApiResponse.success("更新成功", baggage));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error("更新失败: " + e.getMessage()));
        }
    }
}

