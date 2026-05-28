package com.example.airplanesalehouduan.admin.controller;


import com.example.airplanesalehouduan.Login.dto.ApiResponse;
import com.example.airplanesalehouduan.admin.entity.AdminOrder;
import com.example.airplanesalehouduan.admin.service.AdminOrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 管理员订单管理控制器
 */
@RestController
@RequestMapping("/api/admin/orders")
public class AdminOrderController {

    @Autowired
    private AdminOrderService adminOrderService;

    /**
     * 获取订单列表（分页）
     */
    @GetMapping
    public ResponseEntity<ApiResponse<?>> getOrderList(
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String orderNumber,
            @RequestParam(required = false) String customer,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate) {
        try {
            Page<AdminOrder> orderPage = adminOrderService.getOrderList(
                    page, size, orderNumber, customer, status, startDate, endDate);

            Map<String, Object> responseData = new HashMap<>();
            responseData.put("orders", orderPage.getContent());
            responseData.put("total", orderPage.getTotalElements());
            responseData.put("page", orderPage.getNumber());
            responseData.put("size", orderPage.getSize());
            responseData.put("totalPages", orderPage.getTotalPages());

            return ResponseEntity.ok(ApiResponse.success("查询成功", responseData));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * 根据ID获取订单详情
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<?>> getOrderById(@PathVariable Long id) {
        try {
            AdminOrder adminOrder = adminOrderService.getOrderById(id)
                    .orElseThrow(() -> new RuntimeException("订单不存在"));
            return ResponseEntity.ok(ApiResponse.success("查询成功", adminOrder));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * 删除订单
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<?>> deleteOrder(@PathVariable Long id) {
        try {
            adminOrderService.deleteOrder(id);
            return ResponseEntity.ok(ApiResponse.success("删除订单成功", null));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }
}

