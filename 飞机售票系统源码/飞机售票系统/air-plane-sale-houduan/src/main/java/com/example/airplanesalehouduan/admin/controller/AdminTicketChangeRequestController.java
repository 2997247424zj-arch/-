package com.example.airplanesalehouduan.admin.controller;

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
 * 管理员改签申请管理控制器
 * 提供改签申请的查询、批准、拒绝等功能
 */
@RestController
@RequestMapping("/api/admin/ticket-change-requests")
public class AdminTicketChangeRequestController {

    @Autowired
    private TicketChangeRequestService ticketChangeRequestService;

    /**
     * 获取改签申请列表（管理员用，支持多条件筛选）
     * GET /api/admin/ticket-change-requests
     * 参数：
     *   - page: 页码（默认0）
     *   - size: 每页大小（默认10）
     *   - status: 状态筛选（可选：待处理、通过、拒绝）
     *   - orderNo: 订单号（可选，模糊查询）
     *   - applicantName: 申请人姓名（可选，模糊查询）
     *   - startDate: 开始日期（可选，格式：yyyy-MM-dd）
     *   - endDate: 结束日期（可选，格式：yyyy-MM-dd）
     */
    @GetMapping
    public ResponseEntity<ApiResponse<?>> getChangeRequestList(
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String orderNo,
            @RequestParam(required = false) String applicantName,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate) {
        try {
            Page<TicketChangeRequest> pageResult = ticketChangeRequestService.getAdminChangeRequestList(
                    page, size, status, orderNo, applicantName, startDate, endDate);

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
     * 根据ID获取改签申请详情
     * GET /api/admin/ticket-change-requests/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<TicketChangeRequest>> getChangeRequestById(@PathVariable Long id) {
        try {
            return ticketChangeRequestService.getById(id)
                    .map(request -> ResponseEntity.ok(ApiResponse.success("查询成功", request)))
                    .orElse(ResponseEntity.notFound().build());
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * 调试用：返回数据库中所有改签记录（不分页、不筛选）
     * GET /api/admin/ticket-change-requests/all
     */
    @GetMapping("/all")
    public ResponseEntity<ApiResponse<?>> getAllChangeRequestsForDebug() {
        try {
            List<TicketChangeRequest> all = ticketChangeRequestService.getByOrderNo(null); // fallback, use repository directly if necessary
            // 如果上述方法没有对应实现，尝试调用 service 的 getAdminChangeRequestList 获取全部
            if (all == null) {
                Page<TicketChangeRequest> page = ticketChangeRequestService.getAdminChangeRequestList(0, Integer.MAX_VALUE, null, null, null, null, null);
                all = page.getContent();
            }
            return ResponseEntity.ok(ApiResponse.success("查询成功", all));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * 批准改签申请
     * POST /api/admin/ticket-change-requests/{id}/approve
     * body: { "processedBy": 1, "remark": "同意改签" }
     *
     * 批准后会：
     * 1. 更新改签申请状态为"通过"
     * 2. 在orders表中创建新订单（使用新航班信息）
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
            String newOrderNo = safeBody.get("newOrderNo") != null
                    ? safeBody.get("newOrderNo").toString()
                    : null;

            TicketChangeRequest updated = ticketChangeRequestService.updateStatus(
                    id, "通过", processedBy, remark, newOrderNo);
            return ResponseEntity.ok(ApiResponse.success("改签申请已批准，新订单已创建", updated));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error("批准失败: " + e.getMessage()));
        }
    }

    /**
     * 拒绝改签申请
     * POST /api/admin/ticket-change-requests/{id}/reject
     * body: { "processedBy": 1, "remark": "拒绝原因" }
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
     * 批量批准改签申请
     * POST /api/admin/ticket-change-requests/batch-approve
     * body: { "ids": [1, 2, 3], "processedBy": 1, "remark": "批量批准" }
     */
    @PostMapping("/batch-approve")
    public ResponseEntity<ApiResponse<?>> batchApprove(
            @RequestBody Map<String, Object> body) {
        try {
            @SuppressWarnings("unchecked")
            List<Integer> ids = (List<Integer>) body.get("ids");
            if (ids == null || ids.isEmpty()) {
                return ResponseEntity.badRequest().body(ApiResponse.error("请选择要批准的申请"));
            }

            Integer processedBy = body.get("processedBy") != null
                    ? Integer.valueOf(body.get("processedBy").toString())
                    : null;
            String remark = body.get("remark") != null
                    ? body.get("remark").toString()
                    : null;

            // 转换为Long类型
            List<Long> longIds = ids.stream().map(Integer::longValue).toList();
            int count = ticketChangeRequestService.batchApprove(longIds, processedBy, remark);

            Map<String, Object> result = new HashMap<>();
            result.put("count", count);
            result.put("total", ids.size());

            return ResponseEntity.ok(ApiResponse.success(
                    String.format("已批量批准 %d 个改签申请", count), result));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error("批量批准失败: " + e.getMessage()));
        }
    }

    /**
     * 批量拒绝改签申请
     * POST /api/admin/ticket-change-requests/batch-reject
     * body: { "ids": [1, 2, 3], "processedBy": 1, "remark": "拒绝原因" }
     */
    @PostMapping("/batch-reject")
    public ResponseEntity<ApiResponse<?>> batchReject(
            @RequestBody Map<String, Object> body) {
        try {
            @SuppressWarnings("unchecked")
            List<Integer> ids = (List<Integer>) body.get("ids");
            if (ids == null || ids.isEmpty()) {
                return ResponseEntity.badRequest().body(ApiResponse.error("请选择要拒绝的申请"));
            }

            Integer processedBy = body.get("processedBy") != null
                    ? Integer.valueOf(body.get("processedBy").toString())
                    : null;
            String remark = body.get("remark") != null
                    ? body.get("remark").toString()
                    : null;

            if (remark == null || remark.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(ApiResponse.error("拒绝原因不能为空"));
            }

            // 转换为Long类型
            List<Long> longIds = ids.stream().map(Integer::longValue).toList();
            int count = ticketChangeRequestService.batchReject(longIds, processedBy, remark);

            Map<String, Object> result = new HashMap<>();
            result.put("count", count);
            result.put("total", ids.size());

            return ResponseEntity.ok(ApiResponse.success(
                    String.format("已批量拒绝 %d 个改签申请", count), result));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error("批量拒绝失败: " + e.getMessage()));
        }
    }

    /**
     * 获取统计数据
     * GET /api/admin/ticket-change-requests/statistics
     */
    @GetMapping("/statistics")
    public ResponseEntity<ApiResponse<?>> getStatistics() {
        try {
            Map<String, Object> stats = ticketChangeRequestService.getStatistics();
            return ResponseEntity.ok(ApiResponse.success("查询成功", stats));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }
}


