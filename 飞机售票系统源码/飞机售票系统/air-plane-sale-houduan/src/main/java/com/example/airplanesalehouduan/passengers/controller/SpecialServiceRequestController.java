package com.example.airplanesalehouduan.passengers.controller;


import com.example.airplanesalehouduan.passengers.entity.SpecialServiceRequest;
import com.example.airplanesalehouduan.passengers.service.SpecialServiceRequestService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * 重点旅客预约控制器
 */
@RestController
@RequestMapping("/api/special-service-requests")
public class SpecialServiceRequestController {

    @Autowired
    private SpecialServiceRequestService service;

    /**
     * 创建重点旅客预约
     */
    @PostMapping
    public ResponseEntity<Map<String, Object>> createRequest(@RequestBody SpecialServiceRequest request) {
        try {
            // 验证必填字段
            if (request.getPassengerId() == null) {
                return ResponseEntity.badRequest().body(createErrorResponse("乘客ID不能为空"));
            }
            if (request.getOrderNo() == null || request.getOrderNo().isEmpty()) {
                return ResponseEntity.badRequest().body(createErrorResponse("订单号不能为空"));
            }
            if (request.getPhone() == null || request.getPhone().isEmpty()) {
                return ResponseEntity.badRequest().body(createErrorResponse("联系电话不能为空"));
            }
            if (request.getPassengerType() == null || request.getPassengerType().isEmpty()) {
                return ResponseEntity.badRequest().body(createErrorResponse("旅客类型不能为空"));
            }
            if (request.getDepartureAirport() == null || request.getDepartureAirport().isEmpty()) {
                return ResponseEntity.badRequest().body(createErrorResponse("出发机场不能为空"));
            }
            if (request.getArrivalAirport() == null || request.getArrivalAirport().isEmpty()) {
                return ResponseEntity.badRequest().body(createErrorResponse("到达机场不能为空"));
            }

            SpecialServiceRequest saved = service.createRequest(request);
            return ResponseEntity.ok(createSuccessResponse("预约申请提交成功", saved));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(createErrorResponse("创建预约失败: " + e.getMessage()));
        }
    }

    /**
     * 根据ID获取预约详情
     */
    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> getById(@PathVariable Long id, @RequestParam(required = false) Integer passengerId) {
        try {
            Optional<SpecialServiceRequest> optional = service.getById(id);
            if (optional.isPresent()) {
                SpecialServiceRequest request = optional.get();

                // 如果提供了passengerId，验证是否为该乘客的预约
                if (passengerId != null && !request.getPassengerId().equals(passengerId)) {
                    return ResponseEntity.status(403).body(createErrorResponse("无权访问该预约"));
                }

                return ResponseEntity.ok(createSuccessResponse("查询成功", request));
            }
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(createErrorResponse("查询失败: " + e.getMessage()));
        }
    }

    /**
     * 根据乘客ID获取预约列表（分页）
     */
    @GetMapping
    public ResponseEntity<Map<String, Object>> getByPassengerId(
            @RequestParam Integer passengerId,
            @RequestParam(required = false, defaultValue = "0") int page,
            @RequestParam(required = false, defaultValue = "10") int size,
            @RequestParam(required = false) String status) {
        try {
            Page<SpecialServiceRequest> pageResult;

            if (status != null && !status.isEmpty()) {
                pageResult = service.getByPassengerIdAndStatus(passengerId, status, page, size);
            } else {
                pageResult = service.getByPassengerId(passengerId, page, size);
            }

            Map<String, Object> data = new HashMap<>();
            data.put("list", pageResult.getContent());
            data.put("total", pageResult.getTotalElements());
            data.put("page", pageResult.getNumber());
            data.put("size", pageResult.getSize());
            data.put("totalPages", pageResult.getTotalPages());

            return ResponseEntity.ok(createSuccessResponse("查询成功", data));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(createErrorResponse("查询失败: " + e.getMessage()));
        }
    }

    /**
     * 根据订单号获取预约
     */
    @GetMapping("/by-order-no/{orderNo}")
    public ResponseEntity<Map<String, Object>> getByOrderNo(
            @PathVariable String orderNo,
            @RequestParam(required = false) Integer passengerId) {
        try {
            List<SpecialServiceRequest> requests;

            if (passengerId != null) {
                Optional<SpecialServiceRequest> optional = service.getByPassengerIdAndOrderNo(passengerId, orderNo);
                if (optional.isPresent()) {
                    requests = List.of(optional.get());
                } else {
                    requests = List.of();
                }
            } else {
                requests = service.getByOrderNo(orderNo);
            }

            return ResponseEntity.ok(createSuccessResponse("查询成功", requests));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(createErrorResponse("查询失败: " + e.getMessage()));
        }
    }

    /**
     * 更新预约状态
     */
    @PutMapping("/{id}/status")
    public ResponseEntity<Map<String, Object>> updateStatus(
            @PathVariable Long id,
            @RequestBody Map<String, String> body) {
        try {
            String status = body.get("status");
            if (status == null || status.isEmpty()) {
                return ResponseEntity.badRequest().body(createErrorResponse("状态不能为空"));
            }

            SpecialServiceRequest updated = service.updateStatus(id, status);
            return ResponseEntity.ok(createSuccessResponse("状态更新成功", updated));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(createErrorResponse("更新失败: " + e.getMessage()));
        }
    }

    /**
     * 更新预约信息
     */
    @PutMapping("/{id}")
    public ResponseEntity<Map<String, Object>> updateRequest(
            @PathVariable Long id,
            @RequestBody SpecialServiceRequest updatedRequest,
            @RequestParam(required = false) Integer passengerId) {
        try {
            Optional<SpecialServiceRequest> optional = service.getById(id);
            if (optional.isEmpty()) {
                return ResponseEntity.notFound().build();
            }

            SpecialServiceRequest existing = optional.get();

            // 如果提供了passengerId，验证是否为该乘客的预约
            if (passengerId != null && !existing.getPassengerId().equals(passengerId)) {
                return ResponseEntity.status(403).body(createErrorResponse("无权修改该预约"));
            }

            SpecialServiceRequest updated = service.updateRequest(id, updatedRequest);
            return ResponseEntity.ok(createSuccessResponse("更新成功", updated));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(createErrorResponse("更新失败: " + e.getMessage()));
        }
    }

    /**
     * 删除预约
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> deleteRequest(
            @PathVariable Long id,
            @RequestParam(required = false) Integer passengerId) {
        try {
            Optional<SpecialServiceRequest> optional = service.getById(id);
            if (optional.isEmpty()) {
                return ResponseEntity.notFound().build();
            }

            SpecialServiceRequest existing = optional.get();

            // 如果提供了passengerId，验证是否为该乘客的预约
            if (passengerId != null && !existing.getPassengerId().equals(passengerId)) {
                return ResponseEntity.status(403).body(createErrorResponse("无权删除该预约"));
            }

            service.deleteRequest(id);
            return ResponseEntity.ok(createSuccessResponse("删除成功", null));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(createErrorResponse("删除失败: " + e.getMessage()));
        }
    }

    /**
     * 统计预约数量
     */
    @GetMapping("/count")
    public ResponseEntity<Map<String, Object>> getCount(
            @RequestParam Integer passengerId,
            @RequestParam(required = false) String status) {
        try {
            long count;
            if (status != null && !status.isEmpty()) {
                count = service.countByPassengerIdAndStatus(passengerId, status);
            } else {
                count = service.countByPassengerId(passengerId);
            }

            Map<String, Object> data = new HashMap<>();
            data.put("count", count);

            return ResponseEntity.ok(createSuccessResponse("查询成功", data));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(createErrorResponse("查询失败: " + e.getMessage()));
        }
    }

    /**
     * 创建成功响应
     */
    private Map<String, Object> createSuccessResponse(String message, Object data) {
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", message);
        response.put("data", data);
        return response;
    }

    /**
     * 创建错误响应
     */
    private Map<String, Object> createErrorResponse(String message) {
        Map<String, Object> response = new HashMap<>();
        response.put("success", false);
        response.put("message", message);
        response.put("data", null);
        return response;
    }
}


