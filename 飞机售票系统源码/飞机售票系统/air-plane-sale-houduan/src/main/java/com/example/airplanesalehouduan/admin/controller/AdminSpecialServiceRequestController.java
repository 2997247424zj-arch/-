package com.example.airplanesalehouduan.admin.controller;

import com.example.airplanesalehouduan.passengers.entity.SpecialServiceRequest;
import com.example.airplanesalehouduan.passengers.service.SpecialServiceRequestService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 管理员 / 运营 对重点旅客预约的管理接口
 */
@RestController
@RequestMapping("/api/admin/special-service-requests")
public class AdminSpecialServiceRequestController {

    @Autowired
    private SpecialServiceRequestService service;

    // 获取列表（按状态）
    @GetMapping
    public ResponseEntity<?> list(@RequestParam(required = false) String status,
                                  @RequestParam(required = false, defaultValue = "0") int page,
                                  @RequestParam(required = false, defaultValue = "20") int size) {
        try {
            Page<SpecialServiceRequest> p = service.getByStatus(status == null ? "" : status, page, size);
            Map<String, Object> data = new HashMap<>();
            data.put("list", p.getContent());
            data.put("total", p.getTotalElements());
            data.put("page", p.getNumber());
            data.put("size", p.getSize());
            data.put("totalPages", p.getTotalPages());
            return ResponseEntity.ok(Map.of("success", true, "message", "查询成功", "data", data));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of("success", false, "message", "查询失败: " + e.getMessage()));
        }
    }

    // 获取分配给某个运营的预约
    @GetMapping("/assigned")
    public ResponseEntity<?> getAssigned(@RequestParam Integer operatorId,
                                         @RequestParam(required = false, defaultValue = "0") int page,
                                         @RequestParam(required = false, defaultValue = "20") int size) {
        try {
            // 简单实现：查询 status = processing OR assigned, 由 repository 支持时可优化
            Page<SpecialServiceRequest> p = service.getByStatus("processing", page, size);
            Map<String, Object> data = new HashMap<>();
            data.put("list", p.getContent());
            data.put("total", p.getTotalElements());
            data.put("page", p.getNumber());
            data.put("size", p.getSize());
            data.put("totalPages", p.getTotalPages());
            return ResponseEntity.ok(Map.of("success", true, "message", "查询成功", "data", data));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of("success", false, "message", "查询失败: " + e.getMessage()));
        }
    }

    @PostMapping("/{id}/approve")
    public ResponseEntity<?> approve(@PathVariable Long id) {
        try {
            SpecialServiceRequest updated = service.updateStatus(id, "approved");
            return ResponseEntity.ok(Map.of("success", true, "message", "已批准", "data", updated));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of("success", false, "message", "批准失败: " + e.getMessage()));
        }
    }

    @PostMapping("/{id}/reject")
    public ResponseEntity<?> reject(@PathVariable Long id, @RequestBody(required = false) Map<String, String> body) {
        try {
            String reason = body == null ? null : body.get("reason");
            SpecialServiceRequest updated = service.updateStatus(id, "rejected");
            return ResponseEntity.ok(Map.of("success", true, "message", "已拒绝", "data", updated));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of("success", false, "message", "拒绝失败: " + e.getMessage()));
        }
    }

    @PostMapping("/{id}/assign")
    public ResponseEntity<?> assign(@PathVariable Long id, @RequestBody Map<String, Integer> body) {
        try {
            Integer operatorId = body.get("operatorId");
            // 目前实体中无 operator 字段，开发时可扩展。这里将状态改为 processing 并返回
            SpecialServiceRequest updated = service.updateStatus(id, "processing");
            return ResponseEntity.ok(Map.of("success", true, "message", "已指派", "data", updated));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of("success", false, "message", "指派失败: " + e.getMessage()));
        }
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<?> updateStatus(@PathVariable Long id, @RequestBody Map<String, String> body) {
        try {
            String status = body.get("status");
            SpecialServiceRequest updated = service.updateStatus(id, status);
            return ResponseEntity.ok(Map.of("success", true, "message", "已更新", "data", updated));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of("success", false, "message", "更新失败: " + e.getMessage()));
        }
    }
}


