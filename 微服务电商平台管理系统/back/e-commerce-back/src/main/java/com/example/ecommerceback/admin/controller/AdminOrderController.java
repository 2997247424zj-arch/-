package com.example.ecommerceback.admin.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.ecommerceback.order.entity.Order;
import com.example.ecommerceback.order.service.OrderService;
import com.example.ecommerceback.utils.Result;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/order")
@RequiredArgsConstructor
public class AdminOrderController {
    
    private final OrderService orderService;
    
    /**
     * 获取所有订单（分页）
     */
    @GetMapping("/list")
    public Result<Page<Order>> getAllOrders(
        @RequestParam(defaultValue = "1") int page,
        @RequestParam(defaultValue = "10") int size,
        @RequestParam(required = false) Integer status,
        @RequestParam(required = false) String keyword,
        HttpSession session
    ) {
        String role = (String) session.getAttribute("role");
        if (!"admin".equals(role)) {
            return Result.error(403, "无权限访问");
        }
        
        return orderService.getAllOrders(page, size, status, keyword);
    }
    
    /**
     * 更新订单状态
     */
    @PutMapping("/status/{orderNo}")
    public Result<String> updateOrderStatus(
        @PathVariable String orderNo,
        @RequestParam Integer status,
        HttpSession session
    ) {
        String role = (String) session.getAttribute("role");
        if (!"admin".equals(role)) {
            return Result.error(403, "无权限访问");
        }
        
        return orderService.adminUpdateOrderStatus(orderNo, status);
    }
    
    /**
     * 获取订单详情
     */
    @GetMapping("/detail/{orderNo}")
    public Result<Object> getOrderDetail(
        @PathVariable String orderNo,
        HttpSession session
    ) {
        String role = (String) session.getAttribute("role");
        if (!"admin".equals(role)) {
            return Result.error(403, "无权限访问");
        }
        
        return orderService.adminGetOrderDetail(orderNo);
    }
    
    /**
     * 删除订单
     */
    @DeleteMapping("/delete/{orderNo}")
    public Result<String> deleteOrder(
        @PathVariable String orderNo,
        HttpSession session
    ) {
        String role = (String) session.getAttribute("role");
        if (!"admin".equals(role)) {
            return Result.error(403, "无权限访问");
        }
        
        return orderService.deleteOrder(orderNo);
    }
}
