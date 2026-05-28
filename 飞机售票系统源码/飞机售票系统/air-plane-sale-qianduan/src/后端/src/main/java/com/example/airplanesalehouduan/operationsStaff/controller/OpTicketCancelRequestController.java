package com.example.airplanesalehouduan.operationsStaff.controller;

import com.example.airplanesalehouduan.operationsStaff.service.OpTicketCancelRequestService;
import com.example.airplanesalehouduan.passengers.entity.TicketCancelRequest;
import org.springframework.data.domain.Page;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 航空运营 - 退票申请管理接口
 *
 * 对接前端 cancelRequestManagementApi：
 *   列表    GET  /api/admin/ticket-cancel-requests
 *   详情    GET  /api/admin/ticket-cancel-requests/{id}
 *   通过    POST /api/admin/ticket-cancel-requests/{id}/approve
 *   拒绝    POST /api/admin/ticket-cancel-requests/{id}/reject
 *   批量通过 POST /api/admin/ticket-cancel-requests/batch-approve
 *   批量拒绝 POST /api/admin/ticket-cancel-requests/batch-reject
 *   统计    GET  /api/admin/ticket-cancel-requests/statistics
 *
 * 注意：虽然 URL 前缀是 /admin，但实现放在 operationsStaff 包下，方便航空运营团队维护。
 */
@RestController
@RequestMapping("/api/admin/ticket-cancel-requests")
public class OpTicketCancelRequestController {

    private final OpTicketCancelRequestService cancelService;

    public OpTicketCancelRequestController(OpTicketCancelRequestService cancelService) {
        this.cancelService = cancelService;
    }

    /**
     * 分页查询退票申请列表
     */
    @GetMapping
    public Map<String, Object> list(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String orderNo,
            @RequestParam(required = false) String applicantName,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate
    ) {
        Page<TicketCancelRequest> resultPage =
                cancelService.queryPage(status, orderNo, applicantName, startDate, endDate, page, size);

        Map<String, Object> data = new HashMap<>();
        data.put("list", resultPage.getContent());
        data.put("total", resultPage.getTotalElements());
        data.put("page", resultPage.getNumber());
        data.put("size", resultPage.getSize());
        data.put("totalPages", resultPage.getTotalPages());

        Map<String, Object> resp = new HashMap<>();
        resp.put("success", true);
        resp.put("data", data);
        return resp;
    }

    /**
     * 根据 ID 获取退票申请详情
     */
    @GetMapping("/{id}")
    public Map<String, Object> getById(@PathVariable Long id) {
        Map<String, Object> resp = new HashMap<>();
        return cancelService.queryPage(null, null, null, null, null, 0, 1)
                .stream()
                .filter(r -> r.getId().equals(id))
                .findFirst()
                .map(r -> {
                    Map<String, Object> ok = new HashMap<>();
                    ok.put("success", true);
                    ok.put("data", r);
                    return ok;
                })
                .orElseGet(() -> {
                    resp.put("success", false);
                    resp.put("message", "退票申请不存在");
                    resp.put("data", null);
                    return resp;
                });
    }

    /**
     * 审核通过
     */
    @PostMapping("/{id}/approve")
    public Map<String, Object> approve(
            @PathVariable Long id,
            @RequestBody(required = false) Map<String, Object> body
    ) throws Exception {
        Integer processedBy = null;
        String remark = null;
        if (body != null) {
            Object processedByObj = body.get("processedBy");
            if (processedByObj != null) {
                processedBy = Integer.valueOf(String.valueOf(processedByObj));
            }
            Object remarkObj = body.get("remark");
            if (remarkObj != null) {
                remark = String.valueOf(remarkObj);
            }
        }

        TicketCancelRequest updated = cancelService.approve(id, processedBy, remark);

        Map<String, Object> resp = new HashMap<>();
        resp.put("success", true);
        resp.put("message", "退票申请已通过");
        resp.put("data", updated);
        return resp;
    }

    /**
     * 审核拒绝
     */
    @PostMapping("/{id}/reject")
    public Map<String, Object> reject(
            @PathVariable Long id,
            @RequestBody Map<String, Object> body
    ) throws Exception {
        Object reasonObj = body.get("remark");
        String reason = reasonObj != null ? String.valueOf(reasonObj) : "退票申请被拒绝";

        Integer processedBy = null;
        Object processedByObj = body.get("processedBy");
        if (processedByObj != null) {
            processedBy = Integer.valueOf(String.valueOf(processedByObj));
        }

        TicketCancelRequest updated = cancelService.reject(id, processedBy, reason);

        Map<String, Object> resp = new HashMap<>();
        resp.put("success", true);
        resp.put("message", "退票申请已拒绝");
        resp.put("data", updated);
        return resp;
    }

    /**
     * 批量通过
     */
    @PostMapping("/batch-approve")
    public Map<String, Object> batchApprove(@RequestBody Map<String, Object> body) {
        List<?> idList = (List<?>) body.get("ids");
        Integer processedBy = null;
        Object processedByObj = body.get("processedBy");
        if (processedByObj != null) {
            processedBy = Integer.valueOf(String.valueOf(processedByObj));
        }
        String remark = null;
        Object remarkObj = body.get("remark");
        if (remarkObj != null) {
            remark = String.valueOf(remarkObj);
        }

        List<Long> ids = idList.stream()
                .map(id -> Long.valueOf(String.valueOf(id)))
                .collect(Collectors.toList());

        cancelService.batchApprove(ids, processedBy, remark);

        Map<String, Object> resp = new HashMap<>();
        resp.put("success", true);
        resp.put("message", "批量通过退票申请成功");
        return resp;
    }

    /**
     * 批量拒绝
     */
    @PostMapping("/batch-reject")
    public Map<String, Object> batchReject(@RequestBody Map<String, Object> body) {
        List<?> idList = (List<?>) body.get("ids");
        String reason = String.valueOf(body.getOrDefault("remark", "退票申请被批量拒绝"));

        Integer processedBy = null;
        Object processedByObj = body.get("processedBy");
        if (processedByObj != null) {
            processedBy = Integer.valueOf(String.valueOf(processedByObj));
        }

        List<Long> ids = idList.stream()
                .map(id -> Long.valueOf(String.valueOf(id)))
                .collect(Collectors.toList());

        cancelService.batchReject(ids, processedBy, reason);

        Map<String, Object> resp = new HashMap<>();
        resp.put("success", true);
        resp.put("message", "批量拒绝退票申请成功");
        return resp;
    }

    /**
     * 统计数据
     */
    @GetMapping("/statistics")
    public Map<String, Object> statistics() {
        OpTicketCancelRequestService.CancelStatistics s = cancelService.getStatistics();

        Map<String, Object> data = new HashMap<>();
        data.put("pending", s.getPending());
        data.put("approved", s.getApproved());
        data.put("rejected", s.getRejected());
        data.put("totalRefundFare", s.getTotalRefundFare());

        Map<String, Object> resp = new HashMap<>();
        resp.put("success", true);
        resp.put("data", data);
        return resp;
    }
}


