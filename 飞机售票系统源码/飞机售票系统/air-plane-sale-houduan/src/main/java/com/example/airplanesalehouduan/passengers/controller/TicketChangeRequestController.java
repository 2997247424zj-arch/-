package com.example.airplanesalehouduan.passengers.controller;

import com.example.airplanesalehouduan.passengers.entity.TicketChangeRequest;
import com.example.airplanesalehouduan.passengers.service.TicketChangeRequestService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * 改签申请控制器（普通乘客 + 运营共用）
 */
@RestController
@RequestMapping("/api/ticket-change-requests")
public class TicketChangeRequestController {

    @Autowired
    private TicketChangeRequestService service;

    /**
     * 乘客提交改签申请（点击“确认改签”）
     * 前端传入：orderno、passengerId、applicantName、旧/新航班信息、费用、reason 等
     * 后端自动生成 changeNo、requestTime、status=待处理，并将原订单状态改为“改签”
     */
    @PostMapping
    public ResponseEntity<Map<String, Object>> createRequest(@RequestBody TicketChangeRequest request) {
        try {
            if (request.getPassengerId() == null) {
                return ResponseEntity.badRequest().body(createErrorResponse("乘客ID不能为空"));
            }
            if (request.getOrderno() == null || request.getOrderno().trim().isEmpty()) {
                return ResponseEntity.badRequest().body(createErrorResponse("订单号不能为空"));
            }
            if (request.getOldFlightNo() == null || request.getOldFlightNo().trim().isEmpty()) {
                return ResponseEntity.badRequest().body(createErrorResponse("原航班号不能为空"));
            }
            if (request.getNewFlightNo() == null || request.getNewFlightNo().trim().isEmpty()) {
                return ResponseEntity.badRequest().body(createErrorResponse("新航班号不能为空"));
            }

            TicketChangeRequest saved = service.createRequest(request);
            return ResponseEntity.ok(createSuccessResponse("改签申请提交成功", saved));
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(createErrorResponse("创建改签申请失败: " + e.getMessage()));
        }
    }

    /**
     * 根据ID获取改签详情
     */
    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> getById(
            @PathVariable Long id,
            @RequestParam(required = false) Integer passengerId) {
        try {
            Optional<TicketChangeRequest> optional = service.getById(id);
            if (optional.isEmpty()) {
                return ResponseEntity.notFound().build();
            }
            TicketChangeRequest request = optional.get();

            // 乘客侧访问时简单做一下权限校验
            if (passengerId != null && !passengerId.equals(request.getPassengerId())) {
                return ResponseEntity.status(403).body(createErrorResponse("无权访问该改签申请"));
            }

            return ResponseEntity.ok(createSuccessResponse("查询成功", request));
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(createErrorResponse("查询失败: " + e.getMessage()));
        }
    }

    /**
     * 乘客分页查看自己的改签记录
     * GET /api/ticket-change-requests?passengerId=1&page=0&size=10&status=待处理
     */
    @GetMapping
    public ResponseEntity<Map<String, Object>> listForPassenger(
            @RequestParam Integer passengerId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String status) {
        try {
            Page<TicketChangeRequest> pageResult = service.getByPassengerId(passengerId, page, size, status);

            Map<String, Object> data = new HashMap<>();
            data.put("list", pageResult.getContent());
            data.put("total", pageResult.getTotalElements());
            data.put("page", pageResult.getNumber());
            data.put("size", pageResult.getSize());
            data.put("totalPages", pageResult.getTotalPages());

            return ResponseEntity.ok(createSuccessResponse("查询成功", data));
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(createErrorResponse("查询失败: " + e.getMessage()));
        }
    }

    /**
     * 根据订单号查询改签记录（乘客或运营都可以用）
     */
    @GetMapping("/by-order-no/{orderNo}")
    public ResponseEntity<Map<String, Object>> getByOrderNo(
            @PathVariable("orderNo") String orderNo,
            @RequestParam(required = false) Integer passengerId) {
        try {
            if (passengerId != null) {
                Optional<TicketChangeRequest> optional =
                        service.getByPassengerIdAndOrderNo(passengerId, orderNo);
                if (optional.isPresent()) {
                    return ResponseEntity.ok(createSuccessResponse("查询成功", optional.get()));
                }
                return ResponseEntity.ok(createSuccessResponse("查询成功", null));
            } else {
                List<TicketChangeRequest> list = service.getByOrderNo(orderNo);
                return ResponseEntity.ok(createSuccessResponse("查询成功", list));
            }
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(createErrorResponse("查询失败: " + e.getMessage()));
        }
    }

    /**
     * 运营审核改签申请，修改状态
     * PUT /api/ticket-change-requests/{id}/status
     * body: { "status": "通过", "processedBy": 1, "remark": "同意改签" }
     */
    @PutMapping("/{id}/status")
    public ResponseEntity<Map<String, Object>> updateStatus(
            @PathVariable Long id,
            @RequestBody(required = false) Map<String, Object> body,
            @RequestParam(required = false) String status) {
        try {
            Map<String, Object> safeBody = body != null ? body : Map.of();

            // 允许从 body 或 query param 读取 status
            Object statusObj = safeBody.get("status");
            String finalStatus = statusObj != null ? String.valueOf(statusObj) : status;
            if (finalStatus == null || finalStatus.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(createErrorResponse("状态不能为空"));
            }

            Integer processedBy = safeBody.get("processedBy") != null
                    ? Integer.valueOf(safeBody.get("processedBy").toString())
                    : null;
            String remark = safeBody.get("remark") != null
                    ? safeBody.get("remark").toString()
                    : null;

            TicketChangeRequest updated = service.updateStatus(id, finalStatus, processedBy, remark);
            return ResponseEntity.ok(createSuccessResponse("状态更新成功", updated));
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(createErrorResponse("更新失败: " + e.getMessage()));
        }
    }

    private Map<String, Object> createSuccessResponse(String message, Object data) {
        Map<String, Object> resp = new HashMap<>();
        resp.put("success", true);
        resp.put("message", message);
        resp.put("data", data);
        return resp;
    }

    private Map<String, Object> createErrorResponse(String message) {
        Map<String, Object> resp = new HashMap<>();
        resp.put("success", false);
        resp.put("message", message);
        resp.put("data", null);
        return resp;
    }
}


