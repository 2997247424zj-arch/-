package com.example.airplanesalehouduan.admin.controller;

import com.example.airplanesalehouduan.Login.dto.ApiResponse;
import com.example.airplanesalehouduan.admin.service.AdminTicketCancelRequestService;
import com.example.airplanesalehouduan.passengers.entity.TicketCancelRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 系统管理员退票审核控制器
 * 专门用于系统管理员审核页面，区别于航空运营管理页面
 * 路径：/api/admin/tickets/refund-review
 */
@RestController
@RequestMapping("/api/admin/tickets/refund-review")
public class AdminTicketCancelReviewController {

    @Autowired
    private AdminTicketCancelRequestService adminTicketCancelRequestService;

    /**
     * 获取退票审核列表（系统管理员审核页面专用）
     * GET /api/admin/tickets/refund-review
     * 参数：
     *   - page: 页码（默认0）
     *   - size: 每页大小（默认10）
     *   - status: 状态筛选（可选：待处理、通过、拒绝）
     */
    @GetMapping
    public ResponseEntity<ApiResponse<?>> getRefundReviewList(
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String status) {
        try {
            Page<TicketCancelRequest> pageResult = adminTicketCancelRequestService.getAdminCancelRequestList(
                    page, size, status, null, null, null, null);

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

    /**
     * 批准退票申请
     * POST /api/admin/tickets/refund-review/{id}/approve
     * body: { "processedBy": 1, "remark": "同意退票" }（可选）
     */
    @PostMapping("/{id}/approve")
    public ResponseEntity<ApiResponse<?>> approveRefund(
            @PathVariable Long id,
            @RequestBody(required = false) Map<String, Object> body) {
        try {
            Map<String, Object> safeBody = body != null ? body : Map.of();
            Integer processedBy = safeBody.get("processedBy") != null
                    ? Integer.valueOf(safeBody.get("processedBy").toString())
                    : null;
            String remark = safeBody.get("remark") != null
                    ? safeBody.get("remark").toString()
                    : null;

            TicketCancelRequest updated = adminTicketCancelRequestService.approveCancelRequest(
                    id, processedBy, remark);
            return ResponseEntity.ok(ApiResponse.success("退票申请已批准", updated));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error("批准失败: " + e.getMessage()));
        }
    }

    /**
     * 拒绝退票申请
     * POST /api/admin/tickets/refund-review/{id}/reject
     * body: { "reason": "拒绝原因", "processedBy": 1 }
     */
    @PostMapping("/{id}/reject")
    public ResponseEntity<ApiResponse<?>> rejectRefund(
            @PathVariable Long id,
            @RequestBody Map<String, Object> body) {
        try {
            String reason = body.get("reason") != null
                    ? body.get("reason").toString()
                    : null;
            Integer processedBy = body.get("processedBy") != null
                    ? Integer.valueOf(body.get("processedBy").toString())
                    : null;

            if (reason == null || reason.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(ApiResponse.error("拒绝原因不能为空"));
            }

            TicketCancelRequest updated = adminTicketCancelRequestService.rejectCancelRequest(
                                id, processedBy, reason);
            return ResponseEntity.ok(ApiResponse.success("退票申请已拒绝", updated));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error("拒绝失败: " + e.getMessage()));
        }
    }

    /**
     * 请求补充材料（退票申请）
     * POST /api/admin/tickets/refund-review/{id}/request-material
     * body: { "message": "需要补充材料说明" }
     */
    @PostMapping("/{id}/request-material")
    public ResponseEntity<ApiResponse<?>> requestMaterial(
            @PathVariable Long id,
            @RequestBody Map<String, Object> body) {
        try {
            String message = body.get("message") != null
                    ? body.get("message").toString()
                    : null;

            if (message == null || message.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(ApiResponse.error("补充材料说明不能为空"));
            }

            // 这里可以更新申请的备注字段，标记需要补充材料
            // 或者创建一个新的补充材料请求记录
            // 暂时只返回成功消息
            Map<String, Object> result = new HashMap<>();
            result.put("id", id);
            result.put("message", message);

            return ResponseEntity.ok(ApiResponse.success("已发送补充材料通知", result));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error("请求补充材料失败: " + e.getMessage()));
        }
    }

    /**
     * 获取退票审核统计数据
     * GET /api/admin/tickets/refund-review/statistics
     * 返回：待审核数量、退票手续费合计等
     */
    @GetMapping("/statistics")
    public ResponseEntity<ApiResponse<?>> getStatistics() {
        try {
            Map<String, Object> stats = adminTicketCancelRequestService.getStatistics();

            // 计算待审核的退票手续费合计
            Page<TicketCancelRequest> pendingPage = adminTicketCancelRequestService.getAdminCancelRequestList(
                    0, Integer.MAX_VALUE, "待处理", null, null, null, null);

            BigDecimal totalRefundFee = pendingPage.getContent().stream()
                    .map(req -> req.getCancelFee() != null ? req.getCancelFee() : BigDecimal.ZERO)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            stats.put("totalRefundFee", totalRefundFee.doubleValue());

            return ResponseEntity.ok(ApiResponse.success("查询成功", stats));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }
}

