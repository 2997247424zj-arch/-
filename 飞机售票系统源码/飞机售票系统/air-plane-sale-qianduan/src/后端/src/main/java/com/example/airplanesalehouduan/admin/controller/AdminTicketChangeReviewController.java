package com.example.airplanesalehouduan.admin.controller;

import com.example.airplanesalehouduan.Login.dto.ApiResponse;
import com.example.airplanesalehouduan.passengers.entity.TicketChangeRequest;
import com.example.airplanesalehouduan.passengers.service.TicketChangeRequestService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

/**
 * 管理员改签审核控制器
 * 专门用于管理员审核页面，区别于运营管理页面
 * 路径：/api/admin/ticket-change-review
 */
@RestController
@RequestMapping("/api/admin/ticket-change-review")
public class AdminTicketChangeReviewController {

    @Autowired
    private TicketChangeRequestService ticketChangeRequestService;

    /**
     * 获取改签审核列表（管理员审核页面专用）
     * GET /api/admin/ticket-change-review
     * 参数：
     *   - page: 页码（默认0）
     *   - size: 每页大小（默认10）
     *   - status: 状态筛选（可选：待处理、通过、拒绝）
     *   - changeNo: 申请号（可选，模糊查询）
     *   - applicantName: 申请人姓名（可选，模糊查询）
     *   - orderNo: 订单号（可选，模糊查询）
     */
    @GetMapping
    public ResponseEntity<ApiResponse<?>> getChangeReviewList(
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String changeNo,
            @RequestParam(required = false) String applicantName,
            @RequestParam(required = false) String orderNo) {
        try {
            // 如果提供了changeNo，优先使用它作为orderNo查询
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

    /**
     * 批准改签申请
     * POST /api/admin/ticket-change-review/{id}/approve
     */
    @PostMapping("/{id}/approve")
    public ResponseEntity<ApiResponse<?>> approveChangeRequest(
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

            TicketChangeRequest updated = ticketChangeRequestService.updateStatus(
                    id, "通过", processedBy, remark);
            return ResponseEntity.ok(ApiResponse.success("改签申请已批准，新订单已创建", updated));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error("批准失败: " + e.getMessage()));
        }
    }

    /**
     * 拒绝改签申请
     * POST /api/admin/ticket-change-review/{id}/reject
     */
    @PostMapping("/{id}/reject")
    public ResponseEntity<ApiResponse<?>> rejectChangeRequest(
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

            if (remark == null || remark.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(ApiResponse.error("拒绝原因不能为空"));
            }

            TicketChangeRequest updated = ticketChangeRequestService.updateStatus(
                    id, "拒绝", processedBy, remark);
            return ResponseEntity.ok(ApiResponse.success("改签申请已拒绝", updated));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error("拒绝失败: " + e.getMessage()));
        }
    }

    /**
     * 获取改签审核统计数据
     * GET /api/admin/ticket-change-review/statistics
     * 返回：待审核数量、改签手续费合计等
     */
    @GetMapping("/statistics")
    public ResponseEntity<ApiResponse<?>> getStatistics(
            @RequestParam(required = false) String status) {
        try {
            Map<String, Object> stats = new HashMap<>();

            // 获取待审核数量
            long pendingCount = (long) ticketChangeRequestService.getStatistics().getOrDefault("pending", 0L);
            stats.put("pendingCount", pendingCount);

            // 获取改签手续费合计（仅统计待处理的）
            // 这里需要查询待处理的改签申请并计算手续费总和
            Page<TicketChangeRequest> pendingPage = ticketChangeRequestService.getAdminChangeRequestList(
                    0, Integer.MAX_VALUE, "待处理", null, null, null, null);

            BigDecimal totalChangeFee = pendingPage.getContent().stream()
                    .map(req -> req.getChangeFee() != null ? req.getChangeFee() : BigDecimal.ZERO)
                    .reduce(BigDecimal.ZERO, (sum, fee) -> sum.add(fee));

            stats.put("totalChangeFee", totalChangeFee.doubleValue());

            return ResponseEntity.ok(ApiResponse.success("查询成功", stats));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }
}

