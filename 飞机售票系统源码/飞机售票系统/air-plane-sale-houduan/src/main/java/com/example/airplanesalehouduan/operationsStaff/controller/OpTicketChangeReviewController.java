package com.example.airplanesalehouduan.operationsStaff.controller;

import com.example.airplanesalehouduan.Login.dto.ApiResponse;
import com.example.airplanesalehouduan.passengers.entity.TicketChangeRequest;
import com.example.airplanesalehouduan.passengers.service.TicketChangeRequestService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 运营人员改签审核控制器（与管理员审核类似，但用于运营页面）
 * 路径：/api/operations/ticket-change-review
 */
@RestController
@RequestMapping("/api/operations/ticket-change-review")
public class OpTicketChangeReviewController {

    @Autowired
    private TicketChangeRequestService ticketChangeRequestService;

    @GetMapping
    public ResponseEntity<ApiResponse<?>> getChangeReviewList(
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String changeNo,
            @RequestParam(required = false) String applicantName,
            @RequestParam(required = false) String orderNo) {
        try {
            String searchOrderNo = changeNo != null && !changeNo.trim().isEmpty() ? changeNo : orderNo;
            Page<TicketChangeRequest> pageResult = ticketChangeRequestService.getAdminChangeRequestList(
                    page, size, status, searchOrderNo, applicantName, null, null);

            Map<String, Object> responseData = new HashMap<>();
            responseData.put("list", pageResult.getContent());
            responseData.put("total", pageResult.getTotalElements());
            responseData.put("page", pageResult.getNumber());
            responseData.put("size", pageResult.getSize());
            responseData.put("totalPages", pageResult.getTotalPages());
            return ResponseEntity.ok(ApiResponse.success("查询成功", responseData));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @PostMapping("/{id}/approve")
    public ResponseEntity<ApiResponse<?>> approveChange(
            @PathVariable Long id,
            @RequestBody(required = false) Map<String, Object> body) {
        try {
            Map<String, Object> safeBody = body != null ? body : Map.of();
            Integer processedBy = safeBody.get("processedBy") != null
                    ? Integer.valueOf(safeBody.get("processedBy").toString())
                    : null;
            String remark = safeBody.get("remark") != null ? safeBody.get("remark").toString() : null;
            String newOrderNo = safeBody.get("newOrderNo") != null ? safeBody.get("newOrderNo").toString() : null;

            TicketChangeRequest updated = ticketChangeRequestService.updateStatus(id, "通过", processedBy, remark, newOrderNo);
            return ResponseEntity.ok(ApiResponse.success("改签申请已批准", updated));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error("批准失败: " + e.getMessage()));
        }
    }

    @PostMapping("/{id}/reject")
    public ResponseEntity<ApiResponse<?>> rejectChange(
            @PathVariable Long id,
            @RequestBody(required = false) Map<String, Object> body) {
        try {
            Map<String, Object> safeBody = body != null ? body : Map.of();
            Integer processedBy = safeBody.get("processedBy") != null
                    ? Integer.valueOf(safeBody.get("processedBy").toString())
                    : null;
            String remark = safeBody.get("remark") != null ? safeBody.get("remark").toString() : null;
            if (remark == null || remark.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(ApiResponse.error("拒绝原因不能为空"));
            }
            TicketChangeRequest updated = ticketChangeRequestService.updateStatus(id, "拒绝", processedBy, remark);
            return ResponseEntity.ok(ApiResponse.success("改签申请已拒绝", updated));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error("拒绝失败: " + e.getMessage()));
        }
    }

    @PostMapping("/batch-approve")
    public ResponseEntity<ApiResponse<?>> batchApprove(@RequestBody Map<String, Object> body) {
        try {
            @SuppressWarnings("unchecked")
            List<Integer> ids = (List<Integer>) body.get("ids");
            if (ids == null || ids.isEmpty()) {
                return ResponseEntity.badRequest().body(ApiResponse.error("请选择要批准的申请"));
            }
            Integer processedBy = body.get("processedBy") != null ? Integer.valueOf(body.get("processedBy").toString()) : null;
            String remark = body.get("remark") != null ? body.get("remark").toString() : null;
            List<Long> longIds = ids.stream().map(Integer::longValue).toList();
            int count = ticketChangeRequestService.batchApprove(longIds, processedBy, remark);
            Map<String, Object> result = new HashMap<>();
            result.put("count", count);
            result.put("total", ids.size());
            return ResponseEntity.ok(ApiResponse.success(String.format("已批量批准 %d 个改签申请", count), result));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error("批量批准失败: " + e.getMessage()));
        }
    }

    @PostMapping("/batch-reject")
    public ResponseEntity<ApiResponse<?>> batchReject(@RequestBody Map<String, Object> body) {
        try {
            @SuppressWarnings("unchecked")
            List<Integer> ids = (List<Integer>) body.get("ids");
            if (ids == null || ids.isEmpty()) {
                return ResponseEntity.badRequest().body(ApiResponse.error("请选择要拒绝的申请"));
            }
            Integer processedBy = body.get("processedBy") != null ? Integer.valueOf(body.get("processedBy").toString()) : null;
            String remark = body.get("remark") != null ? body.get("remark").toString() : null;
            if (remark == null || remark.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(ApiResponse.error("拒绝原因不能为空"));
            }
            List<Long> longIds = ids.stream().map(Integer::longValue).toList();
            int count = ticketChangeRequestService.batchReject(longIds, processedBy, remark);
            Map<String, Object> result = new HashMap<>();
            result.put("count", count);
            result.put("total", ids.size());
            return ResponseEntity.ok(ApiResponse.success(String.format("已批量拒绝 %d 个改签申请", count), result));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error("批量拒绝失败: " + e.getMessage()));
        }
    }
}


