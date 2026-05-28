package com.example.airplanesalehouduan.admin.controller;

import com.example.airplanesalehouduan.Login.dto.ApiResponse;
import com.example.airplanesalehouduan.admin.dto.AdminAlertDto;
import com.example.airplanesalehouduan.admin.service.AdminAlertsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping({"/api/admin/alerts", "/admin/alerts"})
public class AdminAlertsController {

    @Autowired
    private AdminAlertsService adminAlertsService;

    @GetMapping("")
    public ResponseEntity<ApiResponse<?>> getAlerts(
            @org.springframework.web.bind.annotation.RequestParam(name = "page", required = false, defaultValue = "0") int page,
            @org.springframework.web.bind.annotation.RequestParam(name = "size", required = false, defaultValue = "20") int size,
            @org.springframework.web.bind.annotation.RequestParam(name = "status", required = false) String statusFilter) {
        try {
            List<AdminAlertDto> list = adminAlertsService.getLatestAlerts(page, size, statusFilter);
            return ResponseEntity.ok(ApiResponse.success("查询成功", list));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * 上报新的异常事件（管理员或系统上报）
     */
    @PostMapping("")
    public ResponseEntity<ApiResponse<?>> createAlert(@RequestBody java.util.Map<String, Object> body) {
        try {
            long id = adminAlertsService.createException(body);
            return ResponseEntity.ok(ApiResponse.success("创建成功", java.util.Map.of("id", id)));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * 更新异常事件状态（例如设置为 processing/resolved 并填写运营备注）
     */
    @PutMapping("/{id}/status")
    public ResponseEntity<ApiResponse<?>> updateAlertStatus(@PathVariable("id") long id, @RequestBody java.util.Map<String, Object> body) {
        try {
            String status = (String) body.get("status");
            // 如果管理员未填写 operatorNote，默认为中文说明“由系统管理处理.”
            String operatorNote = body.getOrDefault("operatorNote", null) == null ? "由系统管理处理." : String.valueOf(body.get("operatorNote"));
            adminAlertsService.updateExceptionStatus(id, status, operatorNote);
            return ResponseEntity.ok(ApiResponse.success("更新成功", java.util.Collections.emptyMap()));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }
}


