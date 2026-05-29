package com.example.vuecourseproject.cashier.controller;

import com.example.vuecourseproject.cashier.entity.Order;
import com.example.vuecourseproject.cashier.entity.OrderItem; // 正确导入
import com.example.vuecourseproject.cashier.service.OrderService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/orders")
public class OrderController {
    private final OrderService orderService;
    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    // 下单接口
    @PostMapping
    public Order create(@RequestBody OrderCreateRequest request) {
        return orderService.createOrder(request.getOrder(), request.getItems());
    }

    // 查询收银员订单（通过名字）
    @GetMapping("/cashier/{cashierName}")
    public List<Order> getByCashier(@PathVariable String cashierName) {
        return orderService.getOrdersByCashierName(cashierName);
    }

    // DTO
    public static class OrderCreateRequest {
        private Order order;
        private List<OrderItem> items; // 用你自己的OrderItem
        public Order getOrder() { return order; }
        public void setOrder(Order order) { this.order = order; }
        public List<OrderItem> getItems() { return items; }
        public void setItems(List<OrderItem> items) { this.items = items; }
    }
}